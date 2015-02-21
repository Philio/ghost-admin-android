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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import me.philio.ghost.R;
import me.philio.ghost.io.GhostClient;
import me.philio.ghost.io.endpoint.Discovery;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginUrlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginUrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginUrlFragment extends Fragment implements View.OnClickListener,
        View.OnFocusChangeListener, Callback<JsonObject> {

    /**
     * Logging tag
     */
    private static final String TAG = LoginUrlFragment.class.getName();

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

    /**
     * REST client
     */
    private GhostClient mClient = new GhostClient();

    /**
     * Blog URL
     */
    private String mBlogUrl;

    /**
     * Views
     */
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.progressbar)
    ProgressBar mProgressBar;
    @InjectView(R.id.spinner_scheme)
    Spinner mSpinnerScheme;
    @InjectView(R.id.edit_url)
    EditText mEditUrl;
    @InjectView(R.id.txt_url_hint)
    TextView mTxtUrlHint;
    @InjectView(R.id.txt_url_error)
    TextView mTxtUrlError;
    @InjectView(R.id.txt_next)
    TextView mTxtNext;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginUrlFragment newInstance() {
        return new LoginUrlFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_url, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Set title
        mToolbar.setTitle(R.string.title_activity_login);

        // Fix lack of textAllCaps prior to ICS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTxtNext.setText(getString(R.string.action_next).toUpperCase(Locale.getDefault()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure hint is visible after config changes/restored state
        if (mEditUrl.getText().length() > 0) {
            mTxtUrlHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.txt_next)
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.txt_next:
                // Hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Make sure URL is all lower case and remove trailing slashes
                String url = mEditUrl.getText().toString().trim().toLowerCase();
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                mEditUrl.setText(url);

                // Check that the URL looks valid
                if (mEditUrl.getText().toString().isEmpty()) {
                    setUrlError(getString(R.string.error_field_required));
                } else if (!Patterns.WEB_URL.matcher(mEditUrl.getText().toString()).matches()) {
                    setUrlError(getString(R.string.error_invalid_url));
                } else {
                    setUrlError(null);
                    mTxtNext.setEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);

                    // Try and check for a valid ghost install at the URL
                    // We're expecting a 401 with a JSON response
                    mBlogUrl = mSpinnerScheme.getSelectedItem().toString() + mEditUrl.getText().toString();

                    // Run discovery test
                    mClient.setBlogUrl(mBlogUrl);
                    Discovery discovery = mClient.createDiscovery();
                    discovery.test(this);
                }
                break;
        }
    }

    @OnFocusChange(R.id.edit_url)
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_url:
                if (hasFocus) {
                    mTxtUrlHint.setVisibility(View.VISIBLE);
                    mEditUrl.setHint(null);
                } else if (mEditUrl.getText().length() == 0) {
                    mTxtUrlHint.setVisibility(View.INVISIBLE);
                    mEditUrl.setHint(R.string.prompt_blog_url);
                }
                break;
        }
    }

    @OnTextChanged(R.id.edit_url)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && mTxtUrlError.getVisibility() == View.VISIBLE) {
            setUrlError(null);
        }
    }

    @OnItemSelected(R.id.spinner_scheme)
    public void onItemSelected() {
        setUrlError(null);
    }

    @Override
    public void success(JsonObject jsonObject, Response response) {
        // Shouldn't happen!
        setUrlError(getString(R.string.error_invalid_url));
    }

    @Override
    public void failure(RetrofitError error) {
        int status = 0;
        if (error.getResponse() != null) {
            status = error.getResponse().getStatus();
        }
        switch (status) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                // Got a redirect
                Log.d(TAG, "Url is a redirect!");

                // Get the redirect url and examine to attempt to provide most
                // useful error message
                String redirectUrl = null;
                for (Header header : error.getResponse().getHeaders()) {
                    if (header.getName() == null) {
                        continue;
                    }
                    if (header.getName().equals("Location")) {
                        String value = header.getValue();
                        if (value.endsWith("/ghost/api/v0.1/")) {
                            redirectUrl = value.substring(0, value.length() - 16);
                        } else {
                            redirectUrl = value;
                        }
                    }
                }
                if (redirectUrl != null) {
                    setUrlError(getString(R.string.error_redirect_url_to, redirectUrl));
                } else {
                    setUrlError(getString(R.string.error_redirect_url));
                }
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                // Got a 401 so could be a blog, check that the response is JSON
                Object body = error.getBodyAs(JsonObject.class);
                if (body != null && body instanceof JsonObject) {
                    Log.d(TAG, "Url looks good!");
                    mListener.onValidUrl(mBlogUrl);
                } else {
                    setUrlError(getString(R.string.error_invalid_url));
                }
                break;
            default:
                setUrlError(getString(R.string.error_invalid_url));
                break;
        }
        mTxtNext.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Set an error for the URL entry
     *
     * @param message
     */
    private void setUrlError(String message) {
        mTxtUrlError.setText(message);
        if (message != null) {
            mTxtUrlError.setVisibility(View.VISIBLE);
        } else {
            mTxtUrlError.setVisibility(View.GONE);
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {

        public void onValidUrl(String blogUrl);

    }

}
