/*
 * Copyright 2015 Phil Bayfield
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
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.activeandroid.content.ContentProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.account.AccountConstants;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Post;
import me.philio.ghost.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MarkdownFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarkdownFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {

    /**
     * Arguments
     */
    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_POST_ID = "post_id";

    /**
     * Loader ids
     */
    private static final int LOADER_POST = 100;
    private static final int LOADER_BLOG = 101;
    private static final int LOADER_USER = 102;

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

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
     * Post
     */
    private Post mPost;

    /**
     * Views
     */
    @InjectView(R.id.edit_title)
    protected EditText mEditTitle;
    @InjectView(R.id.edit_content)
    protected EditText mEditContent;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param postId Id of the post to edit
     * @return A new instance of fragment MarkdownFragment.
     */
    public static MarkdownFragment newInstance(Account account, long postId) {
        MarkdownFragment fragment = new MarkdownFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCOUNT, account);
        args.putLong(ARG_POST_ID, postId);
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
            if (args.containsKey(ARG_POST_ID)) {
                mPostId = args.getLong(ARG_POST_ID);
            }
        }

        // Get account manager
        mAccountManager = AccountManager.get(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_markdown, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // If post id was provided, load the record
        if (mPostId > 0) {
            getLoaderManager().initLoader(LOADER_POST, null, this);
        } else if (mAccount != null) {
            getLoaderManager().initLoader(LOADER_BLOG, null, this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_POST:
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(Post.class, mPostId), null,
                        BaseColumns._ID + " = ?", new String[]{Long.toString(mPostId)}, null);
            case LOADER_BLOG:
                String blogUrl = mAccountManager
                        .getUserData(mAccount, AccountConstants.KEY_BLOG_URL);
                String blogEmail = mAccountManager
                        .getUserData(mAccount, AccountConstants.KEY_EMAIL);
                return new CursorLoader(getActivity(), ContentProvider.createUri(Blog.class, null),
                        null, "url = ? AND email = ?", new String[]{blogUrl, blogEmail}, null);
            case LOADER_USER:
                String userEmail = mAccountManager
                        .getUserData(mAccount, AccountConstants.KEY_EMAIL);
                return new CursorLoader(getActivity(), ContentProvider.createUri(User.class, null),
                        null, "blog_id = ? AND email = ?",
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

                    // Enable the UI
                    enableUi();
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
                    getLoaderManager().initLoader(LOADER_USER, null, this);
                }
                break;
            case LOADER_USER:
                if (mPost != null) {
                    // Add user to post
                    cursor.moveToFirst();
                    User user = new User();
                    user.loadFromCursor(cursor);
                    mPost.author = user.id;

                    // Enable the UI
                    enableUi();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Enable the UI
     */
    private void enableUi() {
        if (mPost.title != null) {
            mEditTitle.setText(mPost.title);
        }
        if (mPost.markdown != null) {
            mEditContent.setText(mPost.markdown);
        }
        mEditTitle.setEnabled(true);
        mEditContent.setEnabled(true);
        mEditTitle.addTextChangedListener(this);
        mEditContent.addTextChangedListener(this);
        mListener.onPostChanged(mPost);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mPost.title = mEditTitle.getText().toString();
        mPost.markdown = mEditContent.getText().toString();
        mPost.save();
        mListener.onPostChanged(mPost);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onPostChanged(Post post);

    }

}