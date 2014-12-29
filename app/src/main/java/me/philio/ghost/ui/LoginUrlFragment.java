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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.JsonObject;

import java.net.HttpURLConnection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
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
     * Blog URL
     */
    private String mBlogUrl;

    /**
     * Views
     */
    @InjectView(R.id.layout_url_background)
    RelativeLayout mUrlBackground;
    @InjectView(R.id.spinner_scheme)
    Spinner mSpinnerScheme;
    @InjectView(R.id.edit_url)
    EditText mEditUrl;
    @InjectView(R.id.btn_validate)
    Button mBtnValidate;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Note inflater bug with v21, using activity layout inflater
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_login_url, container, false);
        ButterKnife.inject(this, view);
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.btn_validate)
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_validate:
                // Make sure URL is all lower case and remove trailing slashes
                String url = mEditUrl.getText().toString().trim().toLowerCase();
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                mEditUrl.setText(url);

                // Check that the URL looks valid
                if (mEditUrl.getText().toString().isEmpty()) {
                    mEditUrl.setError(getString(R.string.error_field_required));
                } else if (!Patterns.WEB_URL.matcher(mEditUrl.getText().toString()).matches()) {
                    mEditUrl.setError(getString(R.string.error_invalid_url));
                } else {
                    mEditUrl.setError(null);
                    mBtnValidate.setEnabled(false);
                    ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(true);

                    // Try and check for a valid ghost install at the URL
                    // We're expecting a 401 with a JSON response
                    mBlogUrl = mSpinnerScheme.getSelectedItem().toString() + mEditUrl.getText().toString();

                    // Run discovery test
                    GhostClient client = new GhostClient(mBlogUrl);
                    Discovery discovery = client.createDiscovery();
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
                    mUrlBackground.setBackgroundResource(R.color.grey_lighter);
                } else {
                    mUrlBackground.setBackgroundResource(R.color.grey);
                }
                break;
        }
    }

    @Override
    public void success(JsonObject jsonObject, Response response) {
        // Shouldn't happen!
        mEditUrl.setError(getString(R.string.error_invalid_url));
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
                    mEditUrl.setError(getString(R.string.error_redirect_url_to, redirectUrl));
                } else {
                    mEditUrl.setError(getString(R.string.error_redirect_url));
                }
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                // Got a 401 so could be a blog, check that the response is JSON
                Object body = error.getBodyAs(JsonObject.class);
                if (body != null && body instanceof JsonObject) {
                    Log.d(TAG, "Url looks good!");
                    mListener.onValidUrl(mBlogUrl);
                } else {
                    mEditUrl.setError(getString(R.string.error_invalid_url));
                }
                break;
            default:
                mEditUrl.setError(getString(R.string.error_invalid_url));
                break;
        }
        mBtnValidate.setEnabled(true);
        ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(false);
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
