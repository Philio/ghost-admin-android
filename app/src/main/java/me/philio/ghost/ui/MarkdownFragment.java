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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.activeandroid.content.ContentProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import me.philio.ghost.R;
import me.philio.ghost.model.Post;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MarkdownFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarkdownFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Arguments
     */
    private static final String ARG_POST_ID = "post_id";

    /**
     * Post id
     */
    private long mPostId;

    /**
     * Post model
     */
    private Post mPost;

    /**
     * Views
     */
    @InjectView(R.id.edit_title)
    protected EditText mTitleEdit;
    @InjectView(R.id.edit_content)
    protected EditText mContentEdit;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param postId The id of the post to load
     * @return A new instance of fragment MarkdownFragment.
     */
    public static MarkdownFragment newInstance(long postId) {
        MarkdownFragment fragment = new MarkdownFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_POST_ID)) {
                mPostId = args.getLong(ARG_POST_ID);
            }
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Load the post
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ContentProvider.createUri(Post.class, mPostId), null,
                BaseColumns._ID + " = ?", new String[]{Long.toString(mPostId)}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (mPost == null) {
                data.moveToFirst();
                mPost = new Post();
                mPost.loadFromCursor(data);
                mTitleEdit.setText(mPost.title);
                mContentEdit.setText(mPost.markdown);
            } else {
                // TODO post was already loaded, content changed?
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO need to handle loader reset?
    }

    @OnTextChanged(R.id.edit_content)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String content = s.toString();
    }

}
