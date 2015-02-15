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

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.philio.ghost.R;

/**
 * Base activity for generic functionality shared across activities
 * <p/>
 * Created by phil on 15/12/2014.
 */
public abstract class BaseActivity extends ActionBarActivity {

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
    protected static final int FAB_SOFT_KEYBOARD_DELAY = 500;

    /**
     * Broadcast receiver for network events
     */
    private NetworkStateReceiver mReceiver = new NetworkStateReceiver();

    /**
     * Current network state
     */
    private boolean mNetworkConnected;

    /**
     * ActionBar height, used to calculate the number of pixels for network status alert
     */
    private int mActionBarHeight;

    /**
     * Toolbar
     */
    @InjectView(R.id.toolbar)
    @Optional
    protected Toolbar mToolbar;

    /**
     * Layout for showing network alerts
     */
    @InjectView(R.id.layout_alerts)
    @Optional
    protected LinearLayout mAlerts;

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
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAlerts != null) {
            // Recieve network connectivity change broadcasts
            mReceiver.setInitialState();
            registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onStop() {
        if (mAlerts != null) {
            // Stop receiving broadcasts
            unregisterReceiver(mReceiver);
        }

        super.onStop();
    }

    /**
     * Slide up the network error view
     */
    private void showNetworkError() {
        if (mActionBarHeight == 0) {
            setActionBarHeight();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator animator = ValueAnimator.ofInt(0, mActionBarHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(11)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = mAlerts.getLayoutParams();
                    layoutParams.height = value;
                    mAlerts.setLayoutParams(layoutParams);
                }
            });
            animator.setDuration(750);
            animator.start();
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
                @TargetApi(11)
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
                    .obtainStyledAttributes(new int[]{R.attr.actionBarSize});
            mActionBarHeight = styledAttributes.getDimensionPixelSize(0, 0);
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
            if (state != mNetworkConnected) {
                if (mNetworkConnected) {
                    showNetworkError();
                    mNetworkConnected = false;
                } else {
                    hideNetworkError();
                    mNetworkConnected = true;
                }
            }
        }

        public void setInitialState() {
            mNetworkConnected = isConnected();
            if (!mNetworkConnected) {
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
