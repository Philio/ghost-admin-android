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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import me.philio.ghost.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MarkdownFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarkdownFragment extends Fragment {

    /**
     * Arguments
     */
    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Post title
     */
    private String mTitle;

    /**
     * Post content
     */
    private String mContent;

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
     * @param title   Post title
     * @param content Post markdown content
     * @return A new instance of fragment MarkdownFragment.
     */
    public static MarkdownFragment newInstance(String title, String content) {
        MarkdownFragment fragment = new MarkdownFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
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
            if (args.containsKey(ARG_TITLE)) {
                mTitle = args.getString(ARG_TITLE);
            }
            if (args.containsKey(ARG_CONTENT)) {
                mContent = args.getString(ARG_CONTENT);
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
        if (mTitle != null) {
            mTitleEdit.setText(mTitle);
        }
        if (mContent != null) {
            mContentEdit.setText(mContent);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @OnTextChanged(R.id.edit_title)
    public void onTitleChanged(CharSequence s, int start, int before, int count) {
        // Ignore if nothing has changed
        if (start == 0 && before == 0 && count == 0) {
            return;
        }
        mListener.onTitleChanged(s.toString());
    }

    @OnTextChanged(R.id.edit_content)
    public void onContentChanged(CharSequence s, int start, int before, int count) {
        // Ignore if nothing has changed
        if (start == 0 && before == 0 && count == 0) {
            return;
        }
        mListener.onContentChanged(s.toString());
        Log.d("Start", "" + start);
        Log.d("Before", "" + before);
        Log.d("Count", "" + count);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onTitleChanged(String title);

        public void onContentChanged(String content);

    }

}
