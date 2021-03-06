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

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.philio.ghost.R;

/**
 * Base activity for generic functionality shared across activities
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Logging tag
     */
    private static final String TAG = BaseActivity.class.getName();

    /**
     * Navigation drawer delay
     */
    protected static final int NAV_DRAWER_DELAY = 250;

    /**
     * Delay before revealing the floating action button
     */
    protected static final int FAB_INITIAL_REVEAL_DELAY = 500;
    protected static final int FAB_REVEAL_DELAY = 250;
    protected static final int FAB_SOFT_KEYBOARD_DELAY = 400;

    /**
     * Broadcast receiver for network events
     */
    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    /**
     * Current network state
     */
    private boolean networkConnected;

    /**
     * ActionBar height, used to calculate the number of pixels for network status alert
     */
    private int actionBarHeight;

    /**
     * Toolbar
     */
    @InjectView(R.id.toolbar) @Optional Toolbar toolbar;

    /**
     * Layout for showing network alerts
     */
    @InjectView(R.id.layout_alerts) @Optional LinearLayout alertsLayout;

    /**
     * Active action mode
     */
    protected ActionMode actionMode;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onContentViewSet();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onContentViewSet();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        onContentViewSet();
    }

    /**
     * Perform some setup tasks after child activity has called setContentView, view injection,
     * setup Toolbar, etc.
     */
    protected void onContentViewSet() {
        ButterKnife.inject(this);

        // Set up the toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (alertsLayout != null) {
            // Receive network connectivity change broadcasts
            networkStateReceiver.setInitialState();
            registerReceiver(networkStateReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onStop() {
        if (alertsLayout != null) {
            // Stop receiving broadcasts
            unregisterReceiver(networkStateReceiver);
        }

        super.onStop();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        actionMode = mode;

        // Make sure status bar colour is correct on lollipop up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Resources.Theme theme = getTheme();
            TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark});
            getWindow().setStatusBarColor(getResources().getColor(typedArray.getResourceId(0, 0)));
            typedArray.recycle();
        }
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionMode = null;

        // Restore status bar transparency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }
    }

    /**
     * Finish action mode if it exists
     */
    public void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /**
     * Slide up the network error view
     */
    private void showNetworkError() {
        if (actionBarHeight == 0) {
            setActionBarHeight();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator animator = ValueAnimator.ofInt(0, actionBarHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(11)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = alertsLayout.getLayoutParams();
                    layoutParams.height = value;
                    alertsLayout.setLayoutParams(layoutParams);
                }
            });
            animator.setDuration(750);
            animator.start();
        } else {
            ViewGroup.LayoutParams layoutParams = alertsLayout.getLayoutParams();
            layoutParams.height = actionBarHeight;
            alertsLayout.setLayoutParams(layoutParams);
        }
    }

    /**
     * Slide down the network error view
     */
    private void hideNetworkError() {
        if (actionBarHeight == 0) {
            setActionBarHeight();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator anim = ValueAnimator.ofInt(actionBarHeight, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(11)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = alertsLayout.getLayoutParams();
                    layoutParams.height = value;
                    alertsLayout.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(750);
            anim.start();
        } else {
            ViewGroup.LayoutParams layoutParams = alertsLayout.getLayoutParams();
            layoutParams.height = actionBarHeight;
            alertsLayout.setLayoutParams(layoutParams);
        }
    }

    /**
     * Set the height of the ActionBar
     */
    private void setActionBarHeight() {
        actionBarHeight = 0;
        if (getSupportActionBar() != null) {
            actionBarHeight = getSupportActionBar().getHeight();
        }
        if (actionBarHeight == 0) {
            TypedArray styledAttributes = getTheme()
                    .obtainStyledAttributes(new int[]{R.attr.actionBarSize});
            actionBarHeight = styledAttributes.getDimensionPixelSize(0, 0);
        }
    }

    /**
     * Broadcast receiver to monitor network connectivity and display an alert if connectivity is
     * lost
     */
    private class NetworkStateReceiver extends BroadcastReceiver {

        private ConnectivityManager mConnectivityManager;

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean state = isConnected();
            if (state != networkConnected) {
                if (networkConnected) {
                    showNetworkError();
                    networkConnected = false;
                } else {
                    hideNetworkError();
                    networkConnected = true;
                }
            }
        }

        public void setInitialState() {
            networkConnected = isConnected();
            if (!networkConnected) {
                showNetworkError();
            }
        }

        private boolean isConnected() {
            if (mConnectivityManager == null) {
                mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            }
            return mConnectivityManager.getActiveNetworkInfo() != null &&
                    mConnectivityManager.getActiveNetworkInfo().isConnected();
        }

    }

}
