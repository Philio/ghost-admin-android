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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import me.philio.ghost.R;

public class SplashActivity extends AppCompatActivity implements AccountManagerCallback<Bundle> {

    /**
     * Slash screen delay in ms
     */
    private static final int SPLASH_DELAY_MS = 2000;

    /**
     * Timer to start next activity
     */
    private Timer timer;

    /**
     * The account manager
     */
    private AccountManager accountManager;

    /**
     * If addAccount has been called
     */
    private boolean authRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get account manager
        accountManager = AccountManager.get(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start timer
        if (!authRequested) {
            startTimer();
        }
    }

    @Override
    protected void onStop() {
        // Stop running timer
        stopTimer();

        super.onStop();
    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        // Check if account exists now
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts != null && accounts.length > 0) {
            // Logged in, show main screen
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        // Whatever happened, we're done here
        finish();
    }

    /**
     * Schedule a timer to kick off account/authentication checks after a short delay
     */
    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Check if already logged in
                Account[] accounts = accountManager
                        .getAccountsByType(getString(R.string.account_type));

                if (accounts != null && accounts.length > 0) {
                    // Logged in, show main screen
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Not logged in, start authentication
                    authRequested = true;
                    accountManager.addAccount(getString(R.string.account_type), null, null, null,
                            SplashActivity.this, SplashActivity.this, null);
                }
            }
        }, SPLASH_DELAY_MS);
    }

    /**
     * Stop the timer
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

}
