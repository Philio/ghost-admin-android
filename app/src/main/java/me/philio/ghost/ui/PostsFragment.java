/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.philio.ghost.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.account.AccountConstants;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Post;
import me.philio.ghost.model.PostConflict;
import me.philio.ghost.model.PostDraft;
import me.philio.ghost.ui.widget.BezelImageView;
import me.philio.ghost.util.DateUtils;
import me.philio.ghost.util.ImageUtils;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PostsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemLongClickListener {

    /**
     * Logging tag
     */
    private static final String TAG = PostsFragment.class.getName();

    /**
     * Filters to determine which posts to show
     */
    public static final int SHOW_POSTS = 0x1;
    public static final int SHOW_PAGES = 0x2;
    public static final int SHOW_DRAFTS = 0x4;

    /**
     * Arguments
     */
    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_SHOW = "show";

    /**
     * Loader ids
     */
    private static final int LOADER_BLOG = 100;
    private static final int LOADER_DRAFTS = 101;
    private static final int LOADER_CONFLICTS = 102;
    private static final int LOADER_POSTS = 103;

    /**
     * Icon background colours
     */
    private static final int[] ICON_COLORS = {R.color.red_300, R.color.purple_300, R.color.blue_300,
            R.color.green_300, R.color.orange_300, R.color.blue_grey_300};

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Account manager instance
     */
    private AccountManager mAccountManager;

    /**
     * Active user account
     */
    private Account mAccount;

    /**
     * Posts to show
     */
    private int mShow;

    /**
     * List adapter
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * Blog associated with the account
     */
    private Blog mBlog;

    /**
     * Drafts
     */
    private Map<Post, PostDraft> mDrafts = new HashMap<>();

    /**
     * Conflicts
     */
    private Map<Post, PostConflict> mConflicts = new HashMap<>();

    /**
     * Search term
     */
    private String mSearchTerm;

    /**
     * Swipe to refresh layout
     */
    @InjectView(R.id.swipe_refresh)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Empty list info text
     */
    @InjectView(R.id.txt_empty_info)
    protected TextView mEmptyInfoText;

    /**
     * Contextual action mode
     */
    private ActionMode mActionMode;

    /**
     * Post related to the current action mode
     */
    private Post mActionModePost;

    /**
     * Array of drawables related to position in list
     */
    private SparseArray<Pair<BezelImageView, Drawable>> mPostImages = new SparseArray<>();

    /**
     * Contextual action mode callback
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.post_actions, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.edit:
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            // Clear selection and remove reference
            clearSelection();
            mActionMode = null;
        }

    };

    /**
     * Create a new instance of the fragment
     *
     * @param show Posts to show
     * @return A new instance of the fragment
     */
    public static PostsFragment newInstance(Account account, int show) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCOUNT, account);
        args.putInt(ARG_SHOW, show);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ACCOUNT)) {
                mAccount = args.getParcelable(ARG_ACCOUNT);
            }
            if (args.containsKey(ARG_SHOW)) {
                mShow = args.getInt(ARG_SHOW);
            }
        }

        // Get account manager
        mAccountManager = AccountManager.get(getActivity());

        // Set up list adapater
        String[] from = new String[]{"image", "title", "sync_status", "featured", "published_at"};
        int[] to = new int[]{R.id.img_post, R.id.txt_title, R.id.img_sync, R.id.img_featured,
                R.id.txt_subtitle};
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_post, null, from, to, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                Post post = new Post();
                post.loadFromCursor(cursor);
                switch (view.getId()) {
                    case R.id.img_post:
                        // If post image exists replace placeholder
                        BezelImageView imageView = (BezelImageView) view;
                        if (post.image != null && !post.equals(mActionModePost)) {
                            try {
                                String path = ImageUtils.getUrl(post.blog, post.image);
                                String filename = ImageUtils.getFilename(getActivity(), mBlog,
                                        path);
                                File cover = new File(filename);
                                if (cover.exists()) {
                                    Picasso.with(getActivity()).load(cover).fit().centerCrop()
                                            .into(imageView);
                                }
                            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                Log.e(TAG, "Error loading image");
                            }
                        } else {
                            // Apply colour filter to the background based on the first character
                            // in the post title
                            char firstChar;
                            if (mDrafts.containsKey(post)) {
                                firstChar = mDrafts.get(post).title != null ?
                                        mDrafts.get(post).title.charAt(0) : 0;
                            } else {
                                firstChar = post.title != null ? post.title.charAt(0) : 0;
                            }
                            int charValue = Character.getNumericValue(firstChar);
                            charValue = charValue > 0 ? charValue : 0;
                            int color = ICON_COLORS[charValue % ICON_COLORS.length];
                            imageView.getBackground().setColorFilter(getResources().getColor(color),
                                    PorterDuff.Mode.SRC_ATOP);

                            // Make sure the image resource is correct (if view is recycled)
                            if (post.equals(mActionModePost)) {
                                imageView.setImageResource(R.drawable.ic_action_done);
                            } else {
                                imageView.setImageResource(R.drawable.ic_action_description);
                            }
                        }
                        return true;
                    case R.id.txt_title:
                        // Display the latest draft title if post has local changes
                        if (mDrafts.containsKey(post)) {
                            TextView title = (TextView) view;
                            title.setText(mDrafts.get(post).title);
                            return true;
                        }
                        break;
                    case R.id.img_sync:
                        ImageView sync = (ImageView) view;
                        if (mConflicts.containsKey(post)) {
                            sync.setImageResource(R.drawable.ic_notification_sync_problem);
                            sync.setColorFilter(getResources().getColor(R.color.red_500));
                            sync.setVisibility(View.VISIBLE);
                        } else if (mDrafts.containsKey(post) &&
                                (mDrafts.get(post).revision != post.localRevision ||
                                        mDrafts.get(post).revisionEdit != post.localRevisionEdit)) {
                            sync.setImageResource(R.drawable.ic_notification_sync);
                            sync.setColorFilter(getResources().getColor(R.color.text_secondary));
                            sync.setVisibility(View.VISIBLE);
                        } else {
                            sync.setVisibility(View.GONE);
                        }
                        return true;
                    case R.id.img_featured:
                        ImageView featured = (ImageView) view;
                        if (post.featured) {
                            featured.setColorFilter(getResources().getColor(R.color.text_secondary));
                            featured.setVisibility(View.VISIBLE);
                        } else {
                            featured.setVisibility(View.GONE);
                        }
                        return true;
                    case R.id.txt_subtitle:
                        // Format the subtitle like on the web admin
                        TextView subTitle = (TextView) view;
                        if (post.status == Post.Status.DRAFT) {
                            subTitle.setTextColor(getResources().getColor(R.color.red_500));
                            subTitle.setText(R.string.post_draft);
                            return true;
                        } else if (post.page) {
                            subTitle.setTextColor(getResources().getColor(R.color.text_secondary));
                            subTitle.setText(R.string.post_page);
                            return true;
                        } else {
                            subTitle.setTextColor(getResources().getColor(R.color.text_secondary));
                            subTitle.setText(getString(R.string.post_published_ago,
                                    DateUtils.format(getActivity(), post.publishedAt)));
                            return true;
                        }
                }
                return false;
            }
        });
        setListAdapter(mAdapter);

        // Load posts
        getLoaderManager().initLoader(LOADER_BLOG, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup list
        getListView().setOnItemLongClickListener(this);

        // Setup swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue_500,
                R.color.green_500, R.color.red_500);
        mListener.onSwipeRefreshCreated(mSwipeRefreshLayout);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);

        // TODO App was crashing here because getActivity returned null, needs investigation
        if (getActivity() == null) {
            return;
        }

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity()
                .getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.isEmpty() && !s.equals(mSearchTerm)) {
                    mSearchTerm = s;
                    getLoaderManager().restartLoader(LOADER_POSTS, null, PostsFragment.this);
                }
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        mListener.onSearchExpanded(true);

                        // Change empty text to show search related message
                        mEmptyInfoText.setText(R.string.post_empty_search_info);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mListener.onSearchExpanded(false);

                        // Reset empty text
                        mEmptyInfoText.setText(R.string.post_empty_info);

                        // Reset list
                        if (mSearchTerm != null) {
                            mSearchTerm = null;
                            getLoaderManager().restartLoader(LOADER_POSTS, null, PostsFragment.this);
                        }
                        return true;
                    }

                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        mListener.onSwipeRefreshDestroyed();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mActionMode != null) {
            mActionMode.finish();
            return;
        }

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Post post = new Post();
            post.loadFromCursor((Cursor) getListAdapter().getItem(position));
            mListener.onListItemClick(post, mDrafts.get(post));

            // Reset search
            if (mSearchTerm != null) {
                mSearchTerm = null;
                getLoaderManager().restartLoader(LOADER_POSTS, null, PostsFragment.this);
            }
        }
        clearSelection();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode != null && position == getListView().getCheckedItemPosition()) {
            mActionMode.finish();
            return true;
        }

        // Setup action mode
        mActionMode = ((ActionBarActivity) getActivity())
                .startSupportActionMode(mActionModeCallback);
        mActionModePost = new Post();
        mActionModePost.loadFromCursor((Cursor) getListAdapter().getItem(position));
        getListView().setItemChecked(position, true);

        // Animate the circle
        BezelImageView imageView = (BezelImageView) view.findViewById(R.id.img_post);
        if (mPostImages.get(position) == null) {
            mPostImages.put(position, new Pair<>(imageView, imageView.getDrawable()));
        }
        spinImage(imageView);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_BLOG:
                // Load the blog for the current account
                String blogUrl = mAccountManager
                        .getUserData(mAccount, AccountConstants.KEY_BLOG_URL);
                String blogEmail = mAccountManager
                        .getUserData(mAccount, AccountConstants.KEY_EMAIL);
                return new CursorLoader(getActivity(), ContentProvider.createUri(Blog.class, null),
                        null, "url = ? AND email = ?", new String[]{blogUrl, blogEmail}, null);
            case LOADER_DRAFTS:
                // Load all drafts for the current blog
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(PostDraft.class, null), null, "blog_id = ?",
                        new String[]{mBlog.getId().toString()}, null);
            case LOADER_CONFLICTS:
                // Load all conflicts for the current blog
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(PostConflict.class, null), null, "blog_id = ?",
                        new String[]{mBlog.getId().toString()}, null);
            case LOADER_POSTS:
                // Work out which records to show
                boolean loadPosts = (mShow & SHOW_POSTS) > 0;
                boolean loadPages = (mShow & SHOW_PAGES) > 0;
                boolean loadDrafts = (mShow & SHOW_DRAFTS) > 0;

                // Build the where query
                StringBuilder builder = new StringBuilder();
                builder.append("blog_id = ?");
                if (loadPosts && !loadPages) {
                    builder.append(" AND page = 0");
                } else if (loadPages && !loadPosts) {
                    builder.append(" AND page = 1");
                }
                if (!loadDrafts) {
                    builder.append(" AND status LIKE '");
                    builder.append(Post.Status.PUBLISHED);
                    builder.append("'");
                }
                if (mSearchTerm != null && !mSearchTerm.isEmpty()) {
                    builder.append(String.format(
                            " AND (title LIKE '%%%1$s%%' OR slug LIKE '%%%1$s%%' OR markdown LIKE '%%%1$s%%')",
                            mSearchTerm));
                }

                // Return loader
                return new CursorLoader(getActivity(), ContentProvider.createUri(Post.class, null),
                        new String[]{"*", "1 AS sync_status"}, builder.toString(),
                        new String[]{mBlog.getId().toString()},
                        "status ASC, published_at DESC, created_at DESC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_BLOG:
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    mBlog = new Blog();
                    mBlog.loadFromCursor(cursor);
                }

                // Init other loaders
                getLoaderManager().initLoader(LOADER_DRAFTS, null, this);
                getLoaderManager().initLoader(LOADER_CONFLICTS, null, this);
                getLoaderManager().initLoader(LOADER_POSTS, null, this);
                break;
            case LOADER_DRAFTS:
                mDrafts.clear();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        PostDraft postDraft = new PostDraft();
                        postDraft.loadFromCursor(cursor);
                        mDrafts.put(postDraft.post, postDraft);
                        cursor.moveToNext();
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case LOADER_CONFLICTS:
                mConflicts.clear();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        PostConflict postConflict = new PostConflict();
                        postConflict.loadFromCursor(cursor);
                        mConflicts.put(postConflict.post, postConflict);
                        cursor.moveToNext();
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case LOADER_POSTS:
                mAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onRefresh() {
        mListener.onRefresh();
    }

    private void spinImage(final BezelImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Animator animator = AnimatorInflater.loadAnimator(getActivity(), R.animator.spin_y);
            animator.setTarget(imageView);
            animator.setDuration(100);
            animator.addListener(new AnimatorListenerAdapter() {

                int repetitions;
                Drawable original;

                @Override
                public void onAnimationRepeat(Animator animation) {
                    repetitions++;

                    if (original == null) {
                        original = imageView.getDrawable();
                    }

                    if (repetitions % 2 == 1) {
                        if (imageView.getDrawable().equals(original)) {
                            imageView.setImageResource(R.drawable.ic_action_done);
                        } else {
                            imageView.setImageDrawable(original);
                        }
                    }
                }
            });
            animator.start();
        }
    }

    private void clearSelection() {
        int position = getListView().getCheckedItemPosition();
        getListView().setItemChecked(position, false);
        mActionModePost = null;
        if (mPostImages.get(position) != null) {
            Pair<BezelImageView, Drawable> image = mPostImages.get(position);
            image.first.setImageDrawable(image.second);
            mPostImages.delete(position);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onSwipeRefreshCreated(SwipeRefreshLayout layout);

        public void onSwipeRefreshDestroyed();

        public void onRefresh();

        public void onListItemClick(Post post, PostDraft postDraft);

        public void onSearchExpanded(boolean expanded);

    }

}
