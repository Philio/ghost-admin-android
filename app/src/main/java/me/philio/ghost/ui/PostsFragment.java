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
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Post;
import me.philio.ghost.ui.widget.BezelImageView;
import me.philio.ghost.util.DatabaseUtils;
import me.philio.ghost.util.DateUtils;
import me.philio.ghost.util.ImageUtils;

import static me.philio.ghost.account.AccountConstants.KEY_BLOG_URL;
import static me.philio.ghost.account.AccountConstants.KEY_EMAIL;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PostsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    /**
     * Logging tag
     */
    private static final String TAG = PostsFragment.class.getName();

    /**
     * Filters to deterine which posts to show
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
    private static final int LOADER_LIST = 100;

    /**
     * Icon background colours
     */
    private static final int[] ICON_COLORS = {R.color.red_300, R.color.pink_300, R.color.purple_300,
            R.color.deep_purple_300, R.color.indigo_300, R.color.blue_300, R.color.light_blue_300,
            R.color.cyan_300, R.color.teal_300, R.color.green_300, R.color.light_green_300,
            R.color.lime_300, R.color.yellow_300, R.color.amber_300, R.color.orange_300,
            R.color.deep_orange_300, R.color.brown_300, R.color.grey_300, R.color.blue_grey_300};

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

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
    @InjectView(R.id.text_empty_info)
    protected TextView mEmptyInfoText;

    /**
     * Create a new instance of the fragment
     *
     * @param show Posts to show
     * @return
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

        // Set up list adapater
        String[] from = new String[]{"image", "title", "published_at"};
        int[] to = new int[]{R.id.img_post, R.id.txt_title, R.id.txt_subtitle};
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_post, null, from, to, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                Post post = new Post();
                post.loadFromCursor(cursor);
                switch (view.getId()) {
                    // If post image exists replace placeholder
                    case R.id.img_post:
                        BezelImageView imageView = (BezelImageView) view;
                        if (post.image != null) {
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
                            char firstChar = post.title.charAt(0);
                            int charValue = Character.getNumericValue(firstChar);
                            int color = ICON_COLORS[charValue % ICON_COLORS.length];
                            imageView.getBackground().setColorFilter(getResources().getColor(color),
                                    PorterDuff.Mode.SRC_ATOP);

                            // Make sure the image resource is correct (if view is recycled)
                            imageView.setImageResource(R.drawable.ic_action_action_description);
                        }
                        return true;
                    // Format the subtitle like on the web admin
                    case R.id.txt_subtitle:
                        TextView textView = (TextView) view;
                        if (post.status == Post.Status.DRAFT) {
                            textView.setTextColor(getResources().getColor(R.color.draft));
                            textView.setText(R.string.post_draft);
                            return true;
                        } else if (post.page) {
                            textView.setTextColor(getResources().getColor(R.color.text_secondary));
                            textView.setText(R.string.post_page);
                            return true;
                        } else {
                            textView.setTextColor(getResources().getColor(R.color.text_secondary));
                            textView.setText(getString(R.string.post_published_ago,
                                    DateUtils.format(getActivity(), post.publishedAt)));
                            return true;
                        }
                }
                return false;
            }
        });
        setListAdapter(mAdapter);

        // Get current blog
        AccountManager accountManager = AccountManager.get(getActivity());
        String blogUrl = accountManager.getUserData(mAccount, KEY_BLOG_URL);
        String email = accountManager.getUserData(mAccount, KEY_EMAIL);
        mBlog = DatabaseUtils.getBlog(blogUrl, email);

        // Load posts
        getLoaderManager().initLoader(LOADER_LIST, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_posts, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red_500, R.color.blue_500,
                R.color.green_500, R.color.yellow_500);
        mListener.onSwipeRefreshCreated(mSwipeRefreshLayout);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_posts, menu);

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
                    getLoaderManager().restartLoader(LOADER_LIST, null, PostsFragment.this);
                }
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Change empty text to show search related message
                        mEmptyInfoText.setText(R.string.post_empty_search_info);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Reset empty text
                        mEmptyInfoText.setText(R.string.post_empty_info);

                        // Reset list
                        if (mSearchTerm != null) {
                            mSearchTerm = null;
                            getLoaderManager().restartLoader(LOADER_LIST, null, PostsFragment.this);
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
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Post post = new Post();
            post.loadFromCursor((Cursor) getListAdapter().getItem(position));
            mListener.onListItemClick(post);

            // Reset search
            if (mSearchTerm != null) {
                mSearchTerm = null;
                getLoaderManager().restartLoader(LOADER_LIST, null, PostsFragment.this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        return new CursorLoader(getActivity(), ContentProvider.createUri(Post.class, null), null,
                builder.toString(), new String[]{Long.toString(mBlog.getId())},
                "status ASC, published_at DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onRefresh() {
        mListener.onRefresh();
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

        public void onListItemClick(Post post);

    }

}
