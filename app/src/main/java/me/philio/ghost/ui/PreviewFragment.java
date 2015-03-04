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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.util.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewFragment extends Fragment {

    /**
     * Logging tag
     */
    private static final String TAG = PreviewFragment.class.getName();

    /**
     * Arguments
     */
    private static final String ARG_TITLE = "title";
    private static final String ARG_MARKDOWN = "markdown";
    private static final String ARG_IMAGE = "image";
    private static final String ARG_BLOG_ID = "blog_id";
    private static final String ARG_BLOG_URL = "blog_url";
    private static final String ARG_SHOW_OPTIONS = "show_options";

    /**
     * HTML format
     */
    private static final String HTML_FORMAT = "<html>" +
            "<head><style>img { max-width: 100%%; } body { padding: %dpx; }</style></head>" +
            "<body>%s</body>" +
            "</html>";

    /**
     * Content padding for the webview
     */
    private static final int CONTENT_PADDING = 8;

    /**
     * Post title
     */
    private String mTitle;

    /**
     * Markdown content
     */
    private String mMarkdown;

    /**
     * Post image
     */
    private String mImage;

    /**
     * Blog id
     */
    private long mBlogId;

    /**
     * URL prefix
     */
    private String mBlogUrl;

    /**
     * Show options menu
     */
    private boolean mShowOptions;

    /**
     * Instance of the AndDown markdown parser
     */
    private AndDown mAndDown = new AndDown();

    /**
     * Views
     */
    @InjectView(R.id.webview)
    protected WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title       Post title
     * @param markdown    Markdown to display
     * @param image       Post image
     * @param blogId      Blog id
     * @param blogUrl     URL of the blog
     * @param showOptions Show options in action bar
     * @return An instance of the fragment
     */
    public static PreviewFragment newInstance(String title, String markdown, String image,
                                              long blogId, String blogUrl, boolean showOptions) {
        Log.e(TAG, "" + blogId);
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MARKDOWN, markdown);
        args.putString(ARG_IMAGE, image);
        args.putLong(ARG_BLOG_ID, blogId);
        args.putString(ARG_BLOG_URL, blogUrl);
        args.putBoolean(ARG_SHOW_OPTIONS, showOptions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_TITLE)) {
                mTitle = args.getString(ARG_TITLE);
            }
            if (args.containsKey(ARG_MARKDOWN)) {
                mMarkdown = args.getString(ARG_MARKDOWN);
            }
            if (args.containsKey(ARG_IMAGE)) {
                mImage = args.getString(ARG_IMAGE);
            }
            if (args.containsKey(ARG_BLOG_ID)) {
                mBlogId = args.getLong(ARG_BLOG_ID);
            }
            if (args.containsKey(ARG_BLOG_URL)) {
                mBlogUrl = args.getString(ARG_BLOG_URL);
            }
            if (args.containsKey(ARG_SHOW_OPTIONS)) {
                mShowOptions = args.getBoolean(ARG_SHOW_OPTIONS);
            }
        }

        setHasOptionsMenu(mShowOptions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Set up web view
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }

        });
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        // Load HTML into webview
        if (mMarkdown != null) {
            loadContent(mMarkdown);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTitle();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.post_actions, menu);
    }

    /**
     * Update the preview
     *
     * @param title    Post title
     * @param markdown Markdown to display
     * @param image    Post image
     * @param blogId   Blog id
     * @param blogUrl  URL of the blog
     */
    public void updatePreview(String title, String markdown, String image, long blogId,
                              String blogUrl) {
        mTitle = title;
        mMarkdown = markdown;
        mImage = image;
        mBlogId = blogId;
        mBlogUrl = blogUrl;

        updateTitle();

        if (mMarkdown != null) {
            loadContent(mMarkdown);
        }
        ;
    }

    private void updateTitle() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(mTitle);
    }

    private void loadContent(String markdown) {
        // Convert to HTML
        String html = mAndDown.markdownToHtml(markdown);

        // Tweak HTML
        html = String.format(HTML_FORMAT, CONTENT_PADDING, html);

        // Check for local copies of images
        Matcher matcher = Pattern.compile("<img.*src=\"([^\"]*)\"[^>]*>").matcher(html);
        while (matcher.find()) {
            try {
                String imageUrl = ImageUtils.getUrl(mBlogUrl, matcher.group(1));
                String filename = ImageUtils.getFilename(getActivity(), mBlogId, imageUrl);
                if (ImageUtils.fileExists(filename)) {
                    html = html.replaceAll(matcher.group(1), "file://" + filename);
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                continue;
            }
        }

        // Add domain prefix for images and links that start with a /
        html = html.replaceAll("<img(.*)src=\"/", "<img$1src=\"" + mBlogUrl + "/");
        html = html.replaceAll("<a(.*)href=\"/", "<a$1href=\"" + mBlogUrl + "/");

        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        }
    }

}
