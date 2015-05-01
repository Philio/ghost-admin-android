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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import me.philio.ghost.R;
import me.philio.ghost.io.ApiConstants;
import me.philio.ghost.io.GhostClient;
import me.philio.ghost.io.endpoint.Authentication;
import me.philio.ghost.io.endpoint.Users;
import me.philio.ghost.model.ErrorResponse;
import me.philio.ghost.model.Token;
import me.philio.ghost.model.User;
import me.philio.ghost.model.UsersContainer;
import me.philio.ghost.util.AccountUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A fragment to login to a Ghost blog
 */
public class LoginFragment extends Fragment implements Callback<Token> {

    /**
     * Logging tag
     */
    private static final String TAG = LoginFragment.class.getName();

    /**
     * Arguments
     */
    private static final String ARG_BLOG_URL = "blog_url";

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
     * Credentials
     */
    private String email;
    private String password;

    /**
     * Views
     */
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.progressbar) ProgressBar progressBar;
    @InjectView(R.id.edit_email) EditText editEmail;
    @InjectView(R.id.txt_email_hint) TextView txtEmailHint;
    @InjectView(R.id.txt_email_error) TextView txtEmailError;
    @InjectView(R.id.edit_password) EditText editPassword;
    @InjectView(R.id.txt_password_hint) TextView txtPasswordHint;
    @InjectView(R.id.txt_password_error) TextView txtPasswordError;
    @InjectView(R.id.txt_next) TextView txtNext;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param blogUrl Blog URL
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(String blogUrl) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BLOG_URL, blogUrl);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_BLOG_URL)) {
                blogUrl = getArguments().getString(ARG_BLOG_URL);
            }
        }

        // Setup client
        ghostClient.setBlogUrl(blogUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Set title
        toolbar.setTitle(blogUrl);

        // Fix lack of textAllCaps prior to ICS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            txtNext.setText(getString(R.string.action_next).toUpperCase(Locale.getDefault()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure hints are visible after config changes/restored state
        if (editEmail.getText().length() > 0) {
            txtEmailHint.setVisibility(View.VISIBLE);
        }
        if (editPassword.getText().length() > 0) {
            txtPasswordHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.txt_next)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_next:
                // Hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Make sure email is lower case
                editEmail.setText(editEmail.getText().toString().trim().toLowerCase());

                // Validate data before requesting an access token
                boolean valid = true;

                // Validate email address
                if (editEmail.getText().toString().isEmpty()) {
                    setError(txtEmailError, getString(R.string.error_field_required));
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString()).matches()) {
                    setError(txtEmailError, getString(R.string.error_invalid_email));
                    valid = false;
                } else if (AccountUtils.accountExists(getActivity(), blogUrl, editEmail.getText().toString())) {
                    setError(txtEmailError, getString(R.string.error_email_exists));
                    valid = false;
                } else {
                    setError(txtEmailError, null);
                }

                //Validate password
                if (editPassword.getText().toString().trim().isEmpty()) {
                    setError(txtPasswordError, getString(R.string.error_field_required));
                    valid = false;
                } else {
                    setError(txtPasswordError, null);
                }

                // If valid request access token
                if (valid) {
                    // Disable button to avoid multiple clicks, show progress bar
                    txtNext.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);

                    // Get credentials for later
                    email = editEmail.getText().toString();
                    password = editPassword.getText().toString();

                    // Request access token
                    Authentication authentication = ghostClient.createAuthentication();
                    authentication.getAccessToken(ApiConstants.GRANT_TYPE_PASSWORD,
                            ApiConstants.CLIENT_ID, email, password, this);
                }
                break;
        }
    }

    @OnFocusChange({R.id.edit_email, R.id.edit_password})
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_email:
                if (hasFocus) {
                    txtEmailHint.setVisibility(View.VISIBLE);
                    editEmail.setHint(null);
                } else if (editEmail.getText().length() == 0) {
                    txtEmailHint.setVisibility(View.INVISIBLE);
                    editEmail.setHint(R.string.prompt_email);
                }
                break;
            case R.id.edit_password:
                if (hasFocus) {
                    txtPasswordHint.setVisibility(View.VISIBLE);
                    editPassword.setHint(null);
                } else if (editPassword.getText().length() == 0) {
                    txtPasswordHint.setVisibility(View.INVISIBLE);
                    editPassword.setHint(R.string.prompt_password);
                }
                break;
        }
    }

    @OnTextChanged(R.id.edit_email)
    public void onEmailTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && txtEmailError.getVisibility() == View.VISIBLE) {
            setError(txtEmailError, null);
        }
    }

    @OnTextChanged(R.id.edit_password)
    public void onPasswordTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && txtPasswordError.getVisibility() == View.VISIBLE) {
            setError(txtPasswordError, null);
        }
    }

    @Override
    public void success(final Token token, Response response) {
        Log.d(TAG, "Authentication successful");

        // Load the user's record
        ghostClient.setAccessToken(token.accessToken);
        Users users = ghostClient.createUsers();
        users.getMe(new Callback<UsersContainer>() {
            @Override
            public void success(UsersContainer usersContainer, Response response) {
                // Hide progress bar
                progressBar.setVisibility(View.INVISIBLE);

                // Notify activity of successful login
                listener.onSuccess(email, password, token, usersContainer.users.get(0));
            }

            @Override
            public void failure(RetrofitError error) {
                // Show error
                setError(txtEmailError, getString(R.string.error_profile));

                // Enable button and hide progress bar
                txtNext.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void failure(RetrofitError error) {
        Log.d(TAG, "Authentication failed");

        // Parse error JSON
        ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);

        // Extract the first error message (at time of writing there is only ever one)
        String errorMsg = null;
        if (errorResponse != null && errorResponse.errors != null && errorResponse.errors.size() > 0) {
            String rawMsg = errorResponse.errors.get(0).message;
            errorMsg = Html.fromHtml(rawMsg).toString();
        }

        // Show user an error
        int status = 0;
        if (error.getResponse() != null) {
            status = error.getResponse().getStatus();
        }
        switch (status) {
            case HttpURLConnection.HTTP_NOT_FOUND:
                setError(txtEmailError, errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                setError(txtEmailError, errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                setError(txtPasswordError, errorMsg != null ? errorMsg : getString(R.string.error_invalid_password));
                break;
        }

        // Enable button and hide progress bar
        txtNext.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Set error text
     *
     * @param message
     */
    private void setError(TextView textView, String message) {
        textView.setText(message);
        if (message != null) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {

        void onSuccess(String email, String password, Token token, User user);

    }

}
