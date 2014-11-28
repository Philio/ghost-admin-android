package me.philio.ghostadmin.ui;

import android.os.Bundle;

import me.philio.ghostadmin.R;
import me.philio.ghostadmin.account.AccountAuthenticatorActionBarActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AccountAuthenticatorActionBarActivity implements
        LoginUrlFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Add login url fragment to request the blog url to login to
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, LoginUrlFragment.newInstance())
                .commit();
    }

    @Override
    public void onValidUrl(String blogUrl) {
        // Replace url fragment with login fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, LoginFragment.newInstance(blogUrl))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSuccess(String email, String password) {
        // TODO
    }

}



