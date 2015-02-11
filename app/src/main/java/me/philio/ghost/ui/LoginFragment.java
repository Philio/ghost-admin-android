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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener,
        View.OnFocusChangeListener, Callback<Token> {

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
    private OnFragmentInteractionListener mListener;

    /**
     * REST client
     */
    private GhostClient mClient;

    /**
     * Blog URL
     */
    private String mBlogUrl;

    /**
     * Credentials
     */
    private String mEmail;
    private String mPassword;

    /**
     * Views
     */
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.progressbar)
    ProgressBar mProgressBar;
    @InjectView(R.id.edit_email)
    EditText mEditEmail;
    @InjectView(R.id.txt_email_hint)
    TextView mTxtEmailHint;
    @InjectView(R.id.txt_email_error)
    TextView mTxtEmailError;
    @InjectView(R.id.edit_password)
    EditText mEditPassword;
    @InjectView(R.id.txt_password_hint)
    TextView mTxtPasswordHint;
    @InjectView(R.id.txt_password_error)
    TextView mTxtPasswordError;
    @InjectView(R.id.txt_next)
    TextView mTxtNext;

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
            if (args.containsKey(ARG_BLOG_URL)) {
                mBlogUrl = getArguments().getString(ARG_BLOG_URL);
            }
        }

        // Setup client
        mClient = new GhostClient(mBlogUrl);
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
        mToolbar.setTitle(mBlogUrl);

        // Fix lack of textAllCaps prior to ICS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTxtNext.setText(getString(R.string.action_next).toUpperCase());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure hints are visible after config changes/restored state
        if (mEditEmail.getText().length() > 0) {
            mTxtEmailHint.setVisibility(View.VISIBLE);
        }
        if (mEditPassword.getText().length() > 0) {
            mTxtPasswordHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.txt_next)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_next:
                // Hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Make sure email is lower case
                mEditEmail.setText(mEditEmail.getText().toString().trim().toLowerCase());

                // Validate data before requesting an access token
                boolean valid = true;

                // Validate email address
                if (mEditEmail.getText().toString().isEmpty()) {
                    setError(mTxtEmailError, getString(R.string.error_field_required));
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(mEditEmail.getText().toString()).matches()) {
                    setError(mTxtEmailError, getString(R.string.error_invalid_email));
                    valid = false;
                } else if (AccountUtils.accountExists(getActivity(), mBlogUrl, mEditEmail.getText().toString())) {
                    setError(mTxtEmailError, getString(R.string.error_email_exists));
                    valid = false;
                } else {
                    setError(mTxtEmailError, null);
                }

                //Validate password
                if (mEditPassword.getText().toString().trim().isEmpty()) {
                    setError(mTxtPasswordError, getString(R.string.error_field_required));
                    valid = false;
                } else {
                    setError(mTxtPasswordError, null);
                }

                // If valid request access token
                if (valid) {
                    // Disable button to avoid multiple clicks, show progress bar
                    mTxtNext.setEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);

                    // Get credentials for later
                    mEmail = mEditEmail.getText().toString();
                    mPassword = mEditPassword.getText().toString();

                    // Request access token
                    Authentication authentication = mClient.createAuthentication();
                    authentication.getAccessToken(ApiConstants.GRANT_TYPE_PASSWORD,
                            ApiConstants.CLIENT_ID, mEmail, mPassword, this);
                }
                break;
        }
    }

    @OnFocusChange({R.id.edit_email, R.id.edit_password})
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_email:
                if (hasFocus) {
                    mTxtEmailHint.setVisibility(View.VISIBLE);
                    mEditEmail.setHint(null);
                } else if (mEditEmail.getText().length() == 0) {
                    mTxtEmailHint.setVisibility(View.INVISIBLE);
                    mEditEmail.setHint(R.string.prompt_email);
                }
                break;
            case R.id.edit_password:
                if (hasFocus) {
                    mTxtPasswordHint.setVisibility(View.VISIBLE);
                    mEditPassword.setHint(null);
                } else if (mEditPassword.getText().length() == 0) {
                    mTxtPasswordHint.setVisibility(View.INVISIBLE);
                    mEditPassword.setHint(R.string.prompt_password);
                }
                break;
        }
    }

    @OnTextChanged(R.id.edit_email)
    public void onEmailTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && mTxtEmailError.getVisibility() == View.VISIBLE) {
            setError(mTxtEmailError, null);
        }
    }

    @OnTextChanged(R.id.edit_password)
    public void onPasswordTextChanged(CharSequence s, int start, int before, int count) {
        if ((before != 0 || count != 0) && mTxtPasswordError.getVisibility() == View.VISIBLE) {
            setError(mTxtPasswordError, null);
        }
    }

    @Override
    public void success(final Token token, Response response) {
        Log.d(TAG, "Authentication successful");

        // Load the user's record
        mClient.setAccessToken(token.accessToken);
        Users users = mClient.createUsers();
        users.getMe(new Callback<UsersContainer>() {
            @Override
            public void success(UsersContainer usersContainer, Response response) {
                // Hide progress bar
                mProgressBar.setVisibility(View.INVISIBLE);

                // Notify activity of successful login
                mListener.onSuccess(mEmail, mPassword, token, usersContainer.users.get(0));
            }

            @Override
            public void failure(RetrofitError error) {
                // Show error
                setError(mTxtEmailError, getString(R.string.error_profile));

                // Enable button and hide progress bar
                mTxtNext.setEnabled(true);
                mProgressBar.setVisibility(View.INVISIBLE);
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
                setError(mTxtEmailError, errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                setError(mTxtEmailError, errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                setError(mTxtPasswordError, errorMsg != null ? errorMsg : getString(R.string.error_invalid_password));
                break;
        }

        // Enable button and hide progress bar
        mTxtNext.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
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

        public void onSuccess(String email, String password, Token token, User user);

    }

}
