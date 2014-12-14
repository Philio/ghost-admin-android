package me.philio.ghostadmin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import me.philio.ghostadmin.R;
import me.philio.ghostadmin.io.ApiConstants;
import me.philio.ghostadmin.io.GhostClient;
import me.philio.ghostadmin.io.endpoint.Authentication;
import me.philio.ghostadmin.model.ErrorResponse;
import me.philio.ghostadmin.model.Token;
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
    @InjectView(R.id.text_blog_url)
    TextView mTextUrl;
    @InjectView(R.id.layout_email_background)
    View mEmailBackground;
    @InjectView(R.id.layout_password_background)
    View mPasswordBackground;
    @InjectView(R.id.edit_email)
    EditText mEditEmail;
    @InjectView(R.id.edit_password)
    EditText mEditPassword;
    @InjectView(R.id.btn_login)
    Button mBtnLogin;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_BLOG_URL)) {
                mBlogUrl = getArguments().getString(ARG_BLOG_URL);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Note inflater bug with v21, using activity layout inflater
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_login, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextUrl.setText(mBlogUrl);
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

    @OnClick(R.id.btn_login)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                // Validate data before requesting an access token
                boolean valid = true;

                // Validate email address
                if (mEditEmail.getText().toString().trim().isEmpty()) {
                    mEditEmail.setError(getString(R.string.error_field_required));
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(mEditEmail.getText().toString()).matches()) {
                    mEditEmail.setError(getString(R.string.error_invalid_email));
                    valid = false;
                } else {
                    mEditEmail.setError(null);
                }

                //Validate password
                if (mEditPassword.getText().toString().trim().isEmpty()) {
                    mEditPassword.setError(getString(R.string.error_field_required));
                    valid = false;
                } else {
                    mEditPassword.setError(null);
                }

                // If valid request access token
                if (valid) {
                    // Disable button to avoid multiple clicks, show progress bar
                    mBtnLogin.setEnabled(false);
                    ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(true);

                    // Get credentials for later
                    mEmail = mEditEmail.getText().toString();
                    mPassword = mEditPassword.getText().toString();

                    // Request access token
                    GhostClient client = new GhostClient(mBlogUrl);
                    Authentication authentication = client.createAuthentication();
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
                    mEmailBackground.setBackgroundResource(R.color.grey_lighter);
                } else {
                    mEmailBackground.setBackgroundResource(R.color.grey);
                }
                break;
            case R.id.edit_password:
                if (hasFocus) {
                    mPasswordBackground.setBackgroundResource(R.color.grey_lighter);
                } else {
                    mPasswordBackground.setBackgroundResource(R.color.grey);
                }
                break;
        }
    }

    @Override
    public void success(Token token, Response response) {
        Log.d(TAG, "Authentication successful");

        // Hide progress bar
        ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(false);

        // Notify activity of successful login
        mListener.onSuccess(mEmail, mPassword, token);
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
                mEditEmail.setError(errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                mEditEmail.setError(errorMsg != null ? errorMsg : getString(R.string.error_incorrect_email));
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                mEditPassword.setError(errorMsg != null ? errorMsg : getString(R.string.error_invalid_password));
                break;
        }

        // Enable button and hide progress bar
        mBtnLogin.setEnabled(true);
        ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(false);
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {

        public void onSuccess(String email, String password, Token token);

    }

}
