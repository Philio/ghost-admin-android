package me.philio.ghostadmin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnFocusChange;
import me.philio.ghostadmin.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener,
        View.OnFocusChangeListener {

    /**
     * Logging tag
     */
    private static final String TAG = LoginFragment.class.getName();

    /**
     * Arguments
     */
    private static final String ARG_BLOG_URL = "blog_url";

    /**
     * Blog URL
     */
    private String mUrl;

    /**
     * Listener
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Views
     */
    @InjectView(R.id.text_blog_url)
    TextView mBlogUrl;
    @InjectView(R.id.layout_email_background)
    View mEmailBackground;
    @InjectView(R.id.layout_password_background)
    View mPasswordBackground;

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
                mUrl = getArguments().getString(ARG_BLOG_URL);
            }
        }

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);
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
        mBlogUrl.setText(mUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View v) {

    }

    @OnFocusChange({R.id.edit_email, R.id.edit_password})
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_email:
                if (hasFocus) {
                    mEmailBackground.setBackgroundResource(R.color.widget_background_active);
                } else {
                    mEmailBackground.setBackgroundResource(R.color.widget_background);
                }
                break;
            case R.id.edit_password:
                if (hasFocus) {
                    mPasswordBackground.setBackgroundResource(R.color.widget_background_active);
                } else {
                    mPasswordBackground.setBackgroundResource(R.color.widget_background);
                }
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {

        public void onSuccess(String email, String password);

    }

}
