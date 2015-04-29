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
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import me.philio.ghost.R;
import me.philio.ghost.io.GhostClient;
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
public class LoginUrlFragment extends Fragment implements Callback<JsonObject> {

    /**
     * Logging tag
     */
    private static final String TAG = LoginUrlFragment.class.getName();

    /**
     * Listener
     */
    private OnFragmentInteractionListener listener;

    /**
     * REST client
     */
    private GhostClient ghostClient = new GhostClient();

    /**
     * Blog URL
     */
    private String blogUrl;

    /**
     * Views
     */
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.progressbar) ProgressBar progressBar;
    @InjectView(R.id.spinner_scheme) Spinner spinnerScheme;
    @InjectView(R.id.edit_url) EditText editUrl;
    @InjectView(R.id.txt_url_hint) TextView txtUrlHint;
    @InjectView(R.id.txt_url_error) TextView txtUrlError;
    @InjectView(R.id.txt_next) TextView txtNext;

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
            listener = (OnFragmentInteractionListener) activity;
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
        toolbar.setTitle(R.string.title_activity_login);

        // Fix lack of textAllCaps prior to ICS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            txtNext.setText(getString(R.string.action_next).toUpperCase(Locale.getDefault()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure hint is visible after config changes/restored state
        if (editUrl.getText().length() > 0) {
            txtUrlHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.txt_next)
    public void onClick(final View v) {
        // Hide keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // Make sure URL is all lower case and remove trailing slashes
        String url = editUrl.getText().toString().trim().toLowerCase();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        editUrl.setText(url);

        // Check that the URL looks valid
        if (editUrl.getText().toString().isEmpty()) {
            setUrlError(getString(R.string.error_field_required));
        } else if (!Patterns.WEB_URL.matcher(editUrl.getText().toString()).matches()) {
            setUrlError(getString(R.string.error_invalid_url));
        } else {
            setUrlError(null);
            txtNext.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Try and check for a valid ghost install at the URL
            // We're expecting a 401 with a JSON response
            blogUrl = spinnerScheme.getSelectedItem().toString() + editUrl.getText().toString();

            // Run discovery test
            ghostClient.setBlogUrl(blogUrl);
            ghostClient.createDiscovery().test(this);
        }
    }

    @OnFocusChange(R.id.edit_url)
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_url:
                if (hasFocus) {
                    txtUrlHint.setVisibility(View.VISIBLE);
                    editUrl.setHint(null);
                } else if (editUrl.getText().length() == 0) {
                    txtUrlHint.setVisibility(View.INVISIBLE);
                    editUrl.setHint(R.string.prompt_blog_url);
                }
                break;
        }
    }

    @OnTextChanged(R.id.edit_url)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && txtUrlError.getVisibility() == View.VISIBLE) {
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
                String redirectUrl = getRedirectUrl(error.getResponse().getHeaders());
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
                    listener.onValidUrl(blogUrl);
                } else {
                    setUrlError(getString(R.string.error_invalid_url));
                }
                break;
            default:
                setUrlError(getString(R.string.error_invalid_url));
                break;
        }
        txtNext.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Get redirect url from Location header if it exists
     *
     * @param headers
     * @return
     */
    private String getRedirectUrl(List<Header> headers) {
        String redirectUrl = null;
        for (Header header : headers) {
            if (header.getName() == null) {
                continue;
            }
            if (header.getName().equals("Location")) {
                String value = header.getValue();
                if (value.contains(GhostClient.BASE_PATH)) {
                    redirectUrl = value.substring(0, value.indexOf(GhostClient.BASE_PATH));
                } else {
                    redirectUrl = value;
                }
            }
        }
        return redirectUrl;
    }

    /**
     * Set an error for the URL entry
     *
     * @param message
     */
    private void setUrlError(String message) {
        txtUrlError.setText(message);
        if (message != null) {
            txtUrlError.setVisibility(View.VISIBLE);
        } else {
            txtUrlError.setVisibility(View.GONE);
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
