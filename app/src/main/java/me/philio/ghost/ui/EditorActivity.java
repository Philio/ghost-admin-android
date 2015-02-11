package me.philio.ghost.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.activeandroid.content.ContentProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.philio.ghost.R;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Post;
import me.philio.ghost.model.User;

import static me.philio.ghost.account.AccountConstants.KEY_BLOG_URL;
import static me.philio.ghost.account.AccountConstants.KEY_EMAIL;

public class EditorActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MarkdownFragment.OnFragmentInteractionListener {

    /**
     * Extras
     */
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_POST_ID = "post_id";

    /**
     * Loader ids
     */
    private static final int LOADER_POST = 100;
    private static final int LOADER_BLOG = 101;
    private static final int LOADER_USER = 102;

    /**
     * Account manager instance
     */
    private AccountManager mAccountManager;

    /**
     * Account of the current blog
     */
    private Account mAccount;

    /**
     * Id of the post to edit
     */
    private long mPostId;

    /**
     * Post database model
     */
    private Post mPost;

    /**
     * The markdown fragment
     */
    private MarkdownFragment mMarkdownFragment;

    /**
     * The preview fragment
     */
    private PreviewFragment mPreviewFragment;

    /**
     * Views
     */
    @InjectView(R.id.viewpager)
    @Optional
    protected ViewPager mViewPager;

    /**
     * Adpater for the view pager
     */
    private EditorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get account manager
        mAccountManager = AccountManager.get(this);

        // Check extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRA_ACCOUNT)) {
                mAccount = extras.getParcelable(EXTRA_ACCOUNT);
            }
            if (extras.containsKey(EXTRA_POST_ID)) {
                mPostId = extras.getLong(EXTRA_POST_ID);
            }
        }

        // Setup viewpager (for mobile)
        if (mViewPager != null) {
            mAdapter = new EditorAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mAdapter);
        }

        // If post id was provided, load the record
        if (mPostId > 0) {
            getSupportLoaderManager().initLoader(LOADER_POST, null, this);
        } else if (mAccount != null) {
            getSupportLoaderManager().initLoader(LOADER_BLOG, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_POST:
                return new CursorLoader(this, ContentProvider.createUri(Post.class, mPostId), null,
                        BaseColumns._ID + " = ?", new String[]{Long.toString(mPostId)}, null);
            case LOADER_BLOG:
                String blogUrl = mAccountManager.getUserData(mAccount, KEY_BLOG_URL);
                String blogEmail = mAccountManager.getUserData(mAccount, KEY_EMAIL);
                return new CursorLoader(this, ContentProvider.createUri(Blog.class, null), null,
                        "url = ? AND email = ?", new String[]{blogUrl, blogEmail}, null);
            case LOADER_USER:
                String userEmail = mAccountManager.getUserData(mAccount, KEY_EMAIL);
                return new CursorLoader(this, ContentProvider.createUri(User.class, null), null,
                        "blog_id = ? AND email = ?",
                        new String[]{Long.toString(mPost.blog.getId()), userEmail}, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_POST:
                if (mPost == null) {
                    // Set up post model
                    cursor.moveToFirst();
                    mPost = new Post();
                    mPost.loadFromCursor(cursor);

                    // Set up the UI
                    setupFragments();
                }
                break;
            case LOADER_BLOG:
                if (mPost == null) {
                    // Create a new post
                    cursor.moveToFirst();
                    Blog blog = new Blog();
                    blog.loadFromCursor(cursor);
                    mPost = new Post();
                    mPost.blog = blog;
                    mPost.status = Post.Status.DRAFT;
                    mPost.markdown = "";

                    // Load the user
                    getSupportLoaderManager().initLoader(LOADER_USER, null, this);
                }
                break;
            case LOADER_USER:
                if (mPost != null) {
                    // Add user to post
                    cursor.moveToFirst();
                    User user = new User();
                    user.loadFromCursor(cursor);
                    mPost.author = user.id;

                    // Set up the UI
                    setupFragments();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onTitleChanged(String title) {
        mPost.title = title;
        mPost.updatedLocally = true;
        mPost.save();
    }

    @Override
    public void onContentChanged(String content) {
        if (mPreviewFragment != null) {
            mPreviewFragment.updateMarkdown(content);
        }
        mPost.markdown = content;
        mPost.updatedLocally = true;
        mPost.save();
    }

    private void setupFragments() {
        // Instantiate the fragments
        mMarkdownFragment = MarkdownFragment.newInstance(mPost.title, mPost.markdown);
        mPreviewFragment = PreviewFragment.newInstance(mPost.markdown, mPost.blog.url, false);

        // Refresh viewpager
        if (mViewPager != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * A simple adapter to drive the {@link ViewPager} on mobiles and portrait tablets
     */
    private class EditorAdapter extends FragmentPagerAdapter {

        public EditorAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mMarkdownFragment;
                case 1:
                    return mPreviewFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return mMarkdownFragment != null && mPreviewFragment != null ? 2 : 0;
        }

    }

}
