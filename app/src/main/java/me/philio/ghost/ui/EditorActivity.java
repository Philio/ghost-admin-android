package me.philio.ghost.ui;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.philio.ghost.R;
import me.philio.ghost.model.Post;

public class EditorActivity extends BaseActivity implements
        MarkdownFragment.OnFragmentInteractionListener {

    /**
     * Extras
     */
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_POST_ID = "post_id";

    /**
     * Account of the current blog
     */
    private Account mAccount;

    /**
     * Id of the post to edit
     */
    private long mPostId;

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

        // Create new preview fragment instance
        mPreviewFragment = PreviewFragment.newInstance(null, null, false);

        // Setup viewpager (for mobile)
        if (mViewPager != null) {
            mAdapter = new EditorAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mAdapter);
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
    public void onPostChanged(Post post) {
        mPreviewFragment.updatePreview(post.markdown, post.blog.url, false);
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
                    return MarkdownFragment.newInstance(mAccount, mPostId);
                case 1:
                    return mPreviewFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}
