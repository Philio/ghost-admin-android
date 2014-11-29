package me.philio.ghostadmin.ui;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghostadmin.R;
import me.philio.ghostadmin.account.AccountAuthenticatorActionBarActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AccountAuthenticatorActionBarActivity implements
        LoginUrlFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener {

    /**
     * Broadcast receiver for network events
     */
    private NetworkStateReceiver mReceiver = new NetworkStateReceiver();

    /**
     * Views
     */
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.progressbar)
    ProgressBar mProgressBar;
    @InjectView(R.id.layout_alerts)
    LinearLayout mAlerts;

    // ActionBar height
    private int mActionBarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        // Setup toolbar
        setSupportActionBar(mToolbar);

        // Add login url fragment to request the blog url to login to
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, LoginUrlFragment.newInstance())
                    .commit();
        }

        // Make sure back is shown if back stack exists
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Recieve network connectivity change broadcasts
        mReceiver.setInitialState();
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        // Stop receiving broadcasts
        unregisterReceiver(mReceiver);

        super.onStop();
    }

    /**
     * Emulate the old ActionBar progress bar functionality
     *
     * @param visible
     */
    public void setToolbarProgressBarVisibility(boolean visible) {
        Log.d(getClass().getName(), "Visibility: " + visible);
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSuccess(String email, String password) {
        // TODO
    }

    /**
     * Slide up the network error view
     */
    private void showNetworkError() {
        if (mActionBarHeight == 0) {
            setActionBarHeight();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator anim = ValueAnimator.ofInt(0, mActionBarHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = mAlerts.getLayoutParams();
                    layoutParams.height = value;
                    mAlerts.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(750);
            anim.start();
        } else {
            ViewGroup.LayoutParams layoutParams = mAlerts.getLayoutParams();
            layoutParams.height = mActionBarHeight;
            mAlerts.setLayoutParams(layoutParams);
        }
    }

    /**
     * Slide down the network error view
     */
    private void hideNetworkError() {
        if (mActionBarHeight == 0) {
            setActionBarHeight();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator anim = ValueAnimator.ofInt(mActionBarHeight, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = mAlerts.getLayoutParams();
                    layoutParams.height = value;
                    mAlerts.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(750);
            anim.start();
        } else {
            ViewGroup.LayoutParams layoutParams = mAlerts.getLayoutParams();
            layoutParams.height = mActionBarHeight;
            mAlerts.setLayoutParams(layoutParams);
        }
    }

    /**
     * Set the height of the ActionBar
     */
    private void setActionBarHeight() {
        mActionBarHeight = getSupportActionBar().getHeight();
        if (mActionBarHeight == 0) {
            TypedArray styledAttributes = getTheme()
                    .obtainStyledAttributes(new int[] { R.attr.actionBarSize });
            mActionBarHeight = styledAttributes.getDimensionPixelSize(0, 0);
        }
    }

    /**
     * Broadcast receiver to monitor network connectivity and display an alert if connectivity is
     * lost
     */
    private class NetworkStateReceiver extends BroadcastReceiver {

        private ConnectivityManager mConnectivityManager;

        private boolean mConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean state = isConnected();
            if (state != mConnected) {
                if (mConnected) {
                    showNetworkError();
                    mConnected = false;
                } else {
                    hideNetworkError();
                    mConnected = true;
                }
            }
        }

        public void setInitialState() {
            mConnected = isConnected();
            if (!mConnected) {
                showNetworkError();
            }
        }

        private boolean isConnected() {
            if (mConnectivityManager == null) {
                mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            }
            if (mConnectivityManager.getActiveNetworkInfo() != null) {
                return mConnectivityManager.getActiveNetworkInfo().isConnected();
            }
            return false;
        }

    }

}



