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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.commonsware.cwac.anddown.AndDown;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.philio.ghost.R;
import me.philio.ghost.model.Post;
import me.philio.ghost.sync.SyncConstants;
import me.philio.ghost.sync.SyncHelper;

public class MainActivity extends BaseActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener,
        FragmentManager.OnBackStackChangedListener, PostsFragment.OnFragmentInteractionListener {

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
     * A handler on the main thread
     */
    private Handler mHandler = new Handler();

    /**
     * Broadcast receiver to get sync status
     */
    private SyncReceiver mReceiver = new SyncReceiver();

    /**
     * Page title
     */
    private String mTitle;

    /**
     * Id of the post displayed in the preview
     */
    private long mPreviewId;

    /**
     * Views
     */
    @InjectView(R.id.btn_add)
    protected ImageButton mAddBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // Find navigation drawer fragment
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer_scrim,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Reveal the add button after a short delay
        showAdd(FAB_INITIAL_REVEAL_DELAY);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                }
                break;
            case R.id.edit:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager()
                            .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra(EditorActivity.EXTRA_POST_ID, mPreviewId);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationDrawerItemSelected(int item) {
        Fragment fragment = null;
        Intent intent = null;
        switch (item) {
            case NavigationDrawerFragment.ITEM_POSTS:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_POSTS | PostsFragment.SHOW_DRAFTS);
                mTitle = getString(R.string.title_posts);
                getSupportActionBar().setTitle(mTitle);
                break;
            case NavigationDrawerFragment.ITEM_PAGES:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_PAGES | PostsFragment.SHOW_DRAFTS);
                mTitle = getString(R.string.title_pages);
                getSupportActionBar().setTitle(mTitle);
                break;
            case NavigationDrawerFragment.ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case NavigationDrawerFragment.ITEM_ABOUT:
                intent = new Intent(this, AboutActivity.class);
                break;
        }

        // Launch the next fragment after a short delay to allow drawer to close
        if (fragment != null) {
            final Fragment finalFragment = fragment;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager()
                                .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.container, finalFragment)
                            .commitAllowingStateLoss();
                }
            }, NAV_DRAWER_DELAY);
            return true;
        }

        // Launch the next activity after a short delay to allow drawer to close
        if (intent != null) {
            final Intent finalIntent = intent;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(finalIntent);
                }
            }, NAV_DRAWER_DELAY);
        }

        return false;
    }

    @OnClick(R.id.btn_add)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra(EditorActivity.EXTRA_ACCOUNT, mNavigationDrawerFragment.getSelectedAccount());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mNavigationDrawerFragment.setDrawerIndicatorEnabled(false);
            hideAdd(FAB_REVEAL_DELAY);
        } else {
            mNavigationDrawerFragment.setDrawerIndicatorEnabled(true);

            // Make sure keyboard is hidden
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content)
                    .getWindowToken(), 0);

            // Restore page title
            getSupportActionBar().setTitle(mTitle);

            // Show create button
            showAdd(FAB_REVEAL_DELAY);
        }
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
    public void onSwipeRefreshDestroyed() {
        mSwipeRefreshLayout = null;
    }

    @Override
    public void onRefresh() {
        SyncHelper.requestSync(mNavigationDrawerFragment.getSelectedAccount(),
                getString(R.string.content_authority));
    }

    @Override
    public void onListItemClick(Post post) {
        mPreviewId = post.getId();

        getSupportActionBar().setTitle(R.string.title_preview);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.container, PreviewFragment.newInstance(post.markdown, post.blog.url,
                        true))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSearchExpanded(boolean expanded) {
        if (expanded) {
            hideAdd(FAB_SOFT_KEYBOARD_DELAY);
        } else {
            showAdd(FAB_SOFT_KEYBOARD_DELAY);
        }
    }

    private void showAdd(int delay) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_zoom_in);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAddBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setStartOffset(delay);
        mAddBtn.startAnimation(animation);
    }

    private void hideAdd(final int delay) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_zoom_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAddBtn.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setStartOffset(delay);
        mAddBtn.startAnimation(animation);
    }

    /**
     * A receiver to process sync status broadcasts
     */
    private class SyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SyncReceiver got action: " + intent.getAction());

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
