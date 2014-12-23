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
package me.philio.ghostadmin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import me.philio.ghostadmin.R;
import me.philio.ghostadmin.model.Blog;
import me.philio.ghostadmin.model.Post;
import me.philio.ghostadmin.ui.widget.BezelImageView;
import me.philio.ghostadmin.util.DatabaseUtils;
import me.philio.ghostadmin.util.DateUtils;
import me.philio.ghostadmin.util.ImageUtils;

import static me.philio.ghostadmin.account.AccountConstants.KEY_BLOG_URL;
import static me.philio.ghostadmin.account.AccountConstants.KEY_EMAIL;

/**
 * A fragment representing a list of Items.
 *
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PostsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
        int[] to = new int[]{R.id.img_post, R.id.text_title, R.id.text_subtitle};
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_post, null,  from, to, 0);
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
                                String filename = ImageUtils.getFilename(getActivity(), mBlog, path);
                                File cover = new File(filename);
                                if (cover.exists()) {
                                    Picasso.with(getActivity()).load(cover).fit().centerCrop()
                                            .into(imageView);
                                }
                            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                Log.e(TAG, "Error loading image");
                            }
                        } else {
                            imageView.setImageResource(R.drawable.ic_action_action_description);
                        }
                        return true;
                    // Format the subtitle like on the web admin
                    case R.id.text_subtitle:
                        TextView textView = (TextView) view;
                        if (post.status == Post.Status.DRAFT) {
                            textView.setTextColor(getResources().getColor(R.color.red));
                            textView.setText(R.string.post_draft);
                            return true;
                        } else if (post.page) {
                            textView.setTextColor(getResources().getColor(R.color.grey));
                            textView.setText(R.string.post_page);
                            return true;
                        } else {
                            textView.setTextColor(getResources().getColor(R.color.grey));
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_posts, container, false);
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
            mListener.onListItemClick(post.getId());
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

        // Return loader
        return new CursorLoader(getActivity(), ContentProvider.createUri(Post.class, null), null,
                builder.toString(), new String[]{Long.toString(mBlog.getId())}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onListItemClick(long id);

    }

}
