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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import me.philio.ghost.R;
import me.philio.ghost.sync.SyncConstants;
import me.philio.ghost.sync.SyncHelper;

public class MainActivity extends BaseActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        PostsFragment.OnFragmentInteractionListener {

    /**
     * Logging tag
     */
    private static final String TAG = MainActivity.class.getName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Swipe refresh layout
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Broadcast receiver to get sync status
     */
    private SyncReceiver mReceiver = new SyncReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer_scrim,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Receive sync events
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncConstants.ACTION_SYNC_STARTED);
        intentFilter.addAction(SyncConstants.ACTION_SYNC_FINISHED);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        // Stop receiving sync events
        unregisterReceiver(mReceiver);

        super.onStop();
    }

    @Override
    public boolean onNavigationDrawerItemSelected(int item) {
        Fragment fragment = null;
        switch (item) {
            case NavigationDrawerFragment.ITEM_POSTS:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_POSTS | PostsFragment.SHOW_DRAFTS);
                break;
            case NavigationDrawerFragment.ITEM_PAGES:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_PAGES | PostsFragment.SHOW_DRAFTS);
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onSwipeRefreshCreated(SwipeRefreshLayout layout) {
        mSwipeRefreshLayout = layout;

        if (ContentResolver.isSyncActive(mNavigationDrawerFragment.getSelectedAccount(),
                getString(R.string.content_authority))) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onSwipeRefreshDestoryed() {
        mSwipeRefreshLayout = null;
    }

    @Override
    public void onRefresh() {
        SyncHelper.requestSync(mNavigationDrawerFragment.getSelectedAccount(),
                getString(R.string.content_authority));
    }

    @Override
    public void onListItemClick(long id) {

    }

    /**
     * A receiver to process sync status broadcasts
     */
    private class SyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got action: " + intent.getAction());

            // If swipe refresh layout is null ignore
            if (mSwipeRefreshLayout == null) {
                return;
            }

            // Ignore accounts other than the selected account
            if (!mNavigationDrawerFragment.getSelectedAccount()
                    .equals(intent.getParcelableExtra(SyncConstants.EXTRA_ACCOUNT))) {
                return;
            }

            // Set toolbar visibility based on intent action
            switch (intent.getAction()) {
                case SyncConstants.ACTION_SYNC_STARTED:
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case SyncConstants.ACTION_SYNC_FINISHED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }

    }

}