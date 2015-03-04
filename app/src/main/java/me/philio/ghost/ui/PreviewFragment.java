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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.ui.widget.ParallaxImageView;
import me.philio.ghost.ui.widget.ParallaxScrollView;
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
    @InjectView(R.id.scrollview)
    protected ParallaxScrollView mScrollView;
    @InjectView(R.id.img_post)
    protected ParallaxImageView mImgPost;
    @InjectView(R.id.view_padding)
    protected View mViewPadding;
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
        // Listen for scroll events
        mScrollView.setOnScrollListener(new ParallaxScrollView.OnScrollListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                if (mImgPost.getVisibility() == View.VISIBLE) {
                    updateActionBarColor(t);
                }
            }
        });
        // Set up web view
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }

        });
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        updateImage();

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
    public void onStop() {
        // Restore action bar colour if necessary
        if (mImgPost.getVisibility() == View.VISIBLE) {
            resetActionBar();
        }

        super.onStop();
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

        mScrollView.scrollTo(0, 0);
        updateTitle();
        updateImage();

        if (mMarkdown != null) {
            loadContent(mMarkdown);
        }
    }

    private void updateTitle() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(mTitle);
    }

    private void updateActionBarColor(int position) {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        // Fling produces negative values, so always use as 0 to prevent flicker
        position = position < 0 ? 0 : position;

        // Calculate alpha amount
        int alpha;
        alpha = (position <= (mImgPost.getHeight() - actionBar.getHeight())) ?
                (int) (255 * (float) position / (mImgPost.getHeight() - actionBar.getHeight())) :
                255;

        // Apply colour to action bar
        int color = getResources().getColor(R.color.primary);
        actionBar.setBackgroundDrawable(
                new ColorDrawable(Color.argb(alpha, Color.red(color), Color.green(color),
                        Color.blue(color))));
    }

    private void resetActionBar() {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        // Restore colour
        int color = getResources().getColor(R.color.primary);
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }

    private void updateImage() {
        if (mImage != null && !mImage.isEmpty()) {
            try {
                String path = ImageUtils.getUrl(mBlogUrl, mImage);
                String filename = ImageUtils.getFilename(getActivity(), mBlogId, path);
                if (ImageUtils.fileExists(filename)) {
                    Picasso.with(getActivity()).load(new File(filename)).fit().centerCrop()
                            .into(mImgPost);
                } else {
                    Picasso.with(getActivity()).load(path).fit().centerCrop().into(mImgPost);
                }
                mViewPadding.setVisibility(View.GONE);
                mImgPost.setVisibility(View.VISIBLE);
                ((ActionBarActivity) getActivity()).getSupportActionBar()
                        .setBackgroundDrawable(new ColorDrawable(getResources()
                                .getColor(android.R.color.transparent)));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                mImgPost.setVisibility(View.GONE);
                mViewPadding.setVisibility(View.VISIBLE);
                resetActionBar();
            }
        } else {
            mImgPost.setVisibility(View.GONE);
            mViewPadding.setVisibility(View.VISIBLE);
            resetActionBar();
        }
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
                String path = ImageUtils.getUrl(mBlogUrl, matcher.group(1));
                String filename = ImageUtils.getFilename(getActivity(), mBlogId, path);
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
