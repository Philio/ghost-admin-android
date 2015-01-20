/*
 * Copyright 2014 Phil Bayfield & Elsa Shen
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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditorFragment extends Fragment {

    /**
     * Modes
     */
    public static final int MODE_VIEW = 0;
    public static final int MODE_EDIT = 1;

    /**
     * Arguments
     */
    private static final String ARG_POST_ID = "post_id";
    private static final String ARG_MODE = "mode";

    /**
     * Post id
     */
    private long mPostId;

    /**
     * Default mode
     */
    private int mMode = MODE_VIEW;

    /**
     * Views
     */
    @InjectView(R.id.viewpager)
    ViewPager mViewPager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param postId The id of the post to load
     * @param mode   The default view mode
     * @return A new instance of fragment EditorFragment.
     */
    public static EditorFragment newInstance(long postId, int mode) {
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        args.putInt(ARG_MODE, mode);
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
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_editor, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (mViewPager != null) {
            mViewPager.setAdapter(new EditorAdapter(getChildFragmentManager()));
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
    }

    /**
     * A very simple adapter to drive the view pager
     */
    private class EditorAdapter extends FragmentPagerAdapter {

        public EditorAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MarkdownFragment.newInstance(mPostId);
                case 1:
                    return HtmlFragment.newInstance(mPostId);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}
