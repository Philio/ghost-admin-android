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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import me.philio.ghost.model.PostDraft;
import me.philio.ghost.model.User;

import static me.philio.ghost.PreferenceConstants.KEY_ACCOUNT_SYNC_DRAFTS;
import static me.philio.ghost.PreferenceConstants.KEY_ACCOUNT_SYNC_PUBLISHED;
import static me.philio.ghost.PreferenceConstants.KEY_SYNC_DRAFTS;
import static me.philio.ghost.PreferenceConstants.KEY_SYNC_PUBLISHED;
import static me.philio.ghost.PreferenceConstants.SYNC_DRAFT_DEFAULT;
import static me.philio.ghost.PreferenceConstants.SYNC_IMMEDIATELY;
import static me.philio.ghost.PreferenceConstants.SYNC_PUBLISHED_DEFAULT;
import static me.philio.ghost.PreferenceConstants.SYNC_USE_GLOBAL;

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
    private static final int LOADER_POST_DRAFT = 101;
    private static final int LOADER_BLOG = 102;
    private static final int LOADER_USER = 103;

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
     * Sync strategy for drafts
     */
    private int mDraftSyncStrategy = SYNC_DRAFT_DEFAULT;

    /**
     * Sync strategy for published articles
     */
    private int mPublishedSyncStrategy = SYNC_PUBLISHED_DEFAULT;

    /**
     * Post
     */
    private Post mPost;

    /**
     * Post draft
     */
    private PostDraft mDraft;

    /**
     * A flag to indicate if revision number has been updated (first change during editing)
     */
    public boolean mRevisionUpdated;

    /**
     * Views
     */
    @InjectView(R.id.edit_title)
    protected EditText mEditTitle;
    @InjectView(R.id.edit_markdown)
    protected EditText mEditMarkdown;

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

        // Populate preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDraftSyncStrategy = Integer.parseInt(
                sharedPreferences.getString(KEY_SYNC_DRAFTS, Integer.toString(SYNC_DRAFT_DEFAULT)));
        mPublishedSyncStrategy = Integer.parseInt(
                sharedPreferences.getString(KEY_SYNC_PUBLISHED,
                        Integer.toString(SYNC_PUBLISHED_DEFAULT)));

        // Override preferences with account preferences where applicable
        String draftAccountSyncStrategy = mAccountManager
                .getUserData(mAccount, KEY_ACCOUNT_SYNC_DRAFTS);
        if (draftAccountSyncStrategy != null &&
                Integer.parseInt(draftAccountSyncStrategy) != SYNC_USE_GLOBAL) {
            mDraftSyncStrategy = Integer.parseInt(draftAccountSyncStrategy);
        }
        String publishedAccountSyncStrategy = mAccountManager
                .getUserData(mAccount, KEY_ACCOUNT_SYNC_PUBLISHED);
        if (publishedAccountSyncStrategy != null &&
                Integer.parseInt(publishedAccountSyncStrategy) != SYNC_USE_GLOBAL) {
            mPublishedSyncStrategy = Integer.parseInt(publishedAccountSyncStrategy);
        }
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
            case LOADER_POST_DRAFT:
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(PostDraft.class, null), null, "post_id = ?",
                        new String[]{Long.toString(mPostId)}, null);
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

                    // Load the draft
                    if (getLoaderManager().getLoader(LOADER_POST_DRAFT) == null) {
                        getLoaderManager().initLoader(LOADER_POST_DRAFT, null, this);
                    }
                } else if (cursor != null && cursor.getCount() == 1) {
                    // Post was updated externally (e.g. by sync)
                    // TODO
                }
                break;
            case LOADER_POST_DRAFT:
                if (mDraft == null) {
                    // Set up the draft model
                    mDraft = new PostDraft();
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        mDraft.loadFromCursor(cursor);
                    } else {
                        mDraft.blog = mPost.blog;
                        mDraft.post = mPost;
                        mDraft.title = mPost.title;
                        mDraft.markdown = mPost.markdown;
                    }

                    // Enable the UI
                    enableUi();
                } else if (cursor != null && cursor.getCount() == 1) {
                    // Draft was updated externally (sync may have added changes while editor was
                    // open but no changes made)
                    // TODO
                }
            case LOADER_BLOG:
                if (mPost == null) {
                    // Load blog
                    cursor.moveToFirst();
                    Blog blog = new Blog();
                    blog.loadFromCursor(cursor);

                    // Create a new post
                    mPost = new Post();
                    mPost.blog = blog;
                    mPost.status = Post.Status.DRAFT;

                    // Create a new post draft
                    mDraft = new PostDraft();
                    mDraft.blog = blog;
                    mDraft.post = mPost;

                    // Load the user
                    if (getLoaderManager().getLoader(LOADER_USER) == null) {
                        getLoaderManager().initLoader(LOADER_USER, null, this);
                    }
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
        if (mDraft.title != null) {
            mEditTitle.setText(mDraft.title);
        }
        if (mDraft.markdown != null) {
            mEditMarkdown.setText(mDraft.markdown);
        }
        mEditTitle.setEnabled(true);
        mEditMarkdown.setEnabled(true);
        mEditTitle.addTextChangedListener(this);
        mEditMarkdown.addTextChangedListener(this);
        mEditMarkdown.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mEditTitle.getText().toString().isEmpty()) {
                    mEditTitle.setText(R.string.editor_default_title);
                }
            }
        });
        mListener.onPostChanged(mPost, mDraft);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Update post record flags if necessary
        if (!mPost.syncLocalChanges &&
                (mPost.status == Post.Status.DRAFT && mDraftSyncStrategy == SYNC_IMMEDIATELY) ||
                (mPost.status == Post.Status.PUBLISHED && mPublishedSyncStrategy == SYNC_IMMEDIATELY)) {
            mPost.syncLocalChanges = true;
            mPost.save(true);
        } else if (mPost.getId() == null) {
            mPost.save();
        }

        // Save the draft
        mDraft.title = mEditTitle.getText().toString();
        mDraft.markdown = mEditMarkdown.getText().toString();
        if (!mRevisionUpdated) {
            mDraft.revision++;
            mDraft.revisionEdit = 0;
        } else {
            mDraft.revisionEdit++;
        }
        mDraft.save();

        // Notify activity that the post has changed
        mListener.onPostChanged(mPost, mDraft);
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

        public void onPostChanged(Post post, PostDraft draft);

    }

}