package me.philio.ghost.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewFragment extends Fragment {

    /**
     * Arguments
     */
    private static final String ARG_MARKDOWN = "markdown";
    private static final String ARG_URL_PREFIX = "url_prefix";
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
     * Markdown content
     */
    private String mMarkdown;

    /**
     * URL prefix
     */
    private String mUrlPrefix;

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
     * @param markdown  Markdown to display
     * @param urlPrefix URL prefix to fix any incomplete URLs
     * @return A new instance of fragment PreviewFragment.
     */
    public static PreviewFragment newInstance(String markdown, String urlPrefix, boolean showOptions) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MARKDOWN, markdown);
        args.putString(ARG_URL_PREFIX, urlPrefix);
        args.putBoolean(ARG_SHOW_OPTIONS, showOptions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_MARKDOWN)) {
                mMarkdown = args.getString(ARG_MARKDOWN);
            }
            if (args.containsKey(ARG_URL_PREFIX)) {
                mUrlPrefix = args.getString(ARG_URL_PREFIX);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_preview, menu);
    }

    public void updatePreview(String markdown, String urlPrefix, boolean showOptions) {
        mMarkdown = markdown;
        mUrlPrefix = urlPrefix;
        mShowOptions = showOptions;

        if (mMarkdown != null) {
            loadContent(mMarkdown);
        }

        setHasOptionsMenu(showOptions);
    }

    private void loadContent(String markdown) {
        // Convert to HTML
        String html = mAndDown.markdownToHtml(markdown);

        // Tweak HTML
        html = String.format(HTML_FORMAT, CONTENT_PADDING, html);

        // Add domain prefix for images and links that start with a /
        html = html.replaceAll("<img(.*)src=\"/", "<img$1src=\"" + mUrlPrefix + "/");
        html = html.replaceAll("<a(.*)href=\"/", "<a$1href=\"" + mUrlPrefix + "/");

        if (mWebView != null) {
            mWebView.loadData(html, "text/html", "UTF-8");
        }
    }

}
