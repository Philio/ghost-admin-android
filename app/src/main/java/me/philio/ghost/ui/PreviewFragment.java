package me.philio.ghost.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

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
    private static final String ARG_HTML = "param1";
    private static final String ARG_URL_PREFIX = "param2";

    /**
     * HTML format
     */
    private static final String HTML_FORMAT = "<html>" +
            "<head><style>img { max-width: 100%%; } body { padding: %dpx; }</style></head>" +
            "<body>%s</body>" +
            "</html>";

    /**
     * HTML content
     */
    private String mHtml;

    /**
     * URL prefix
     */
    private String mUrlPrefix;

    /**
     * Views
     */
    @InjectView(R.id.webview)
    protected WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param html HTML to display in the web view
     * @param urlPrefix URL prefix to fix any incomplete URLs
     * @return A new instance of fragment PreviewFragment.
     */
    public static PreviewFragment newInstance(String html, String urlPrefix) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HTML, html);
        args.putString(ARG_URL_PREFIX, urlPrefix);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_HTML)) {
                mHtml = getArguments().getString(ARG_HTML);
            }
            if (args.containsKey(ARG_URL_PREFIX)) {
                mUrlPrefix = getArguments().getString(ARG_URL_PREFIX);
            }
        }
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

        // Format HTML
        String html = String.format(HTML_FORMAT, 8, mHtml);

        // Add domain prefix for images and links that start with a /
        html = html.replaceAll("<img(.*)src=\"/", "<img$1src=\"" + mUrlPrefix + "/");
        html = html.replaceAll("<a(.*)href=\"/", "<a$1href=\"" + mUrlPrefix + "/");

        mWebView.loadData(html, "text/html", "UTF-8");
        Log.d("HTML", html);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_preview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
