package me.philio.ghostadmin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Timer;
import java.util.TimerTask;

import me.philio.ghostadmin.R;

public class SplashActivity extends ActionBarActivity implements AccountManagerCallback<Bundle> {

    /**
     * Timer to start next activity
     */
    private Timer mTimer;

    /**
     * The account manager
     */
    private AccountManager mAccountManager;

    /**
     * If addAccount has been called
     */
    private boolean mAuthRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get account manager
        mAccountManager = AccountManager.get(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Schedule a timer to kick off account/authentication checks after a short delay
        if (mAuthRequested) {
            return;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Check if already logged in
                Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));

                if (accounts != null && accounts.length > 0) {
                    // Logged in, show main screen
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Not logged in, start authentication
                    mAuthRequested = true;
                    mAccountManager.addAccount(getString(R.string.account_type), null, null, null,
                            SplashActivity.this, SplashActivity.this, null);
                }
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        // Stop any running timer
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        super.onStop();
    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        // Check if account exists now
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts != null && accounts.length > 0) {
            // Logged in, show main screen
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        // Whatever happened, we're done here
        finish();
    }

}
