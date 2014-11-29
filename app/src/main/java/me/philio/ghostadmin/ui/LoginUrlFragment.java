package me.philio.ghostadmin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import me.philio.ghostadmin.R;
import me.philio.ghostadmin.io.GhostClient;
import me.philio.ghostadmin.io.endpoint.Discovery;
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
     * Ghost REST client
     */
    private GhostClient mClient;

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
    Spinner mScheme;
    @InjectView(R.id.edit_url)
    EditText mUrl;
    @InjectView(R.id.btn_validate)
    Button mValidate;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the client
        mClient = new GhostClient();
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
                // Check that the URL looks valid
                if (mUrl.getText().toString().trim().isEmpty()) {
                    mUrl.setError(getString(R.string.error_field_required));
                } else if (!Patterns.WEB_URL.matcher(mUrl.getText().toString()).matches()) {
                    mUrl.setError(getString(R.string.error_invalid_url));
                } else {
                    mUrl.setError(null);
                    mValidate.setEnabled(false);
                    ((LoginActivity) getActivity()).setToolbarProgressBarVisibility(true);

                    // Try and check for a valid ghost install at the URL
                    // We're expecting a 401 with a JSON response
                    mBlogUrl = mScheme.getSelectedItem().toString() + mUrl.getText().toString();
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
                    mUrlBackground.setBackgroundResource(R.color.widget_background_active);
                } else {
                    mUrlBackground.setBackgroundResource(R.color.widget_background);
                }
                break;
        }
    }

    @Override
    public void success(JsonObject jsonObject, Response response) {
        // Shouldn't happen!
        mUrl.setError(getString(R.string.error_invalid_url));
    }

    @Override
    public void failure(RetrofitError error) {
        int status = 0;
        if (error.getResponse() != null) {
            status = error.getResponse().getStatus();
        }
        switch (status) {
            case 301:
            case 302:
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
                    mUrl.setError(getString(R.string.error_redirect_url_to, redirectUrl));
                } else {
                    mUrl.setError(getString(R.string.error_redirect_url));
                }
                break;
            case 401:
                // Got a 401 so could be a blog, check that the response is JSON
                Object body = error.getBodyAs(JsonObject.class);
                if (body != null && body instanceof JsonObject) {
                    Log.d(TAG, "Url looks good!");
                    mListener.onValidUrl(mBlogUrl);
                } else {
                    mUrl.setError(getString(R.string.error_invalid_url));
                }
                break;
            default:
                mUrl.setError(getString(R.string.error_invalid_url));
                break;
        }
        mValidate.setEnabled(true);
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
