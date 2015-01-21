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
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.philio.ghost.R;
import me.philio.ghost.account.AccountConstants;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Setting;
import me.philio.ghost.model.User;
import me.philio.ghost.sync.SyncHelper;
import me.philio.ghost.ui.widget.BezelImageView;
import me.philio.ghost.ui.widget.ScrimInsetsScrollView;
import me.philio.ghost.util.DatabaseUtils;
import me.philio.ghost.util.ImageUtils;

import static me.philio.ghost.account.AccountConstants.KEY_BLOG_URL;
import static me.philio.ghost.account.AccountConstants.KEY_EMAIL;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, AccountManagerCallback<Bundle> {

    /**
     * Logging tag
     */
    private static final String TAG = NavigationDrawerFragment.class.getName();

    /**
     * Remember the selected account
     */
    private static final String STATE_SELECTED_ACCOUNT = "selected_navigation_drawer_account";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * Last account that was used is remembered in a preference
     */
    private static final String PREF_LAST_ACCOUNT = "navigation_drawer_account_name";

    /**
     * Item ids
     */
    private static final int ITEM_DIVIDER = -1;
    public static final int ITEM_POSTS = 0;
    public static final int ITEM_PAGES = 1;
    public static final int ITEM_SETTINGS = 2;
    public static final int ITEM_ABOUT = 3;

    /**
     * Item icons
     */
    private static final int[] ICONS = new int[]{
            R.drawable.ic_action_action_description,
            R.drawable.ic_action_action_description,
            R.drawable.ic_action_action_settings,
            R.drawable.ic_action_action_help
    };

    /**
     * Item titles
     */
    private static final int[] TITLES = new int[]{
            R.string.navigation_drawer_posts,
            R.string.navigation_drawer_pages,
            R.string.navigation_drawer_settings,
            R.string.navigation_drawer_about
    };

    /**
     * Loader ids
     */
    private static final int LOADER_BLOG_NAME = 0;
    private static final int LOADER_USER = 1;

    /**
     * Loader bundle keys
     */
    private static final String LOADER_KEY_BLOG_ID = "blog_id";
    private static final String LOADER_KEY_EMAIL = "email";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * The main drawer layout
     */
    private DrawerLayout mDrawerLayout;

    /**
     * The nav drawer container view
     */
    private View mContainerView;

    /**
     * State/prefs of the drawer
     */
    private int mCurrentSelectedItem = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    /**
     * Account manager
     */
    private AccountManager mAccountManager;

    /**
     * List of accounts
     */
    private List<Account> mAccounts;

    /**
     * Active account
     */
    private Account mSelectedAccount;

    /**
     * Items in the drawer
     */
    private List<Integer> mItems = new ArrayList<>();

    /**
     * Views in the drawer list
     */
    private View[] mItemViews;

    /**
     * Set to true if the account view is visible
     */
    private boolean mAccountListVisible;

    /**
     * Injected views
     */
    @InjectView(R.id.header)
    View mHeader;
    @InjectView(R.id.header_content)
    View mHeaderContent;
    @InjectView(R.id.img_cover)
    ImageView mCover;
    @InjectView(R.id.img_avatar)
    BezelImageView mAvatar;
    @InjectView(R.id.txt_name)
    TextView mName;
    @InjectView(R.id.txt_email)
    TextView mEmail;
    @InjectView(R.id.txt_blog_title)
    TextView mBlogName;
    @InjectView(R.id.txt_blog_url)
    TextView mBlogUrl;
    @InjectView(R.id.img_account_indicator)
    ImageView mAccountIndictor;
    @InjectView(R.id.item_list)
    ViewGroup mItemList;
    @InjectView(R.id.account_list)
    ViewGroup mAccountList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the account manager and get accounts
        mAccountManager = AccountManager.get(getActivity());

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mSelectedAccount = savedInstanceState.getParcelable(STATE_SELECTED_ACCOUNT);
            mCurrentSelectedItem = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Get accounts and set active account
        getAccounts(sp.getString(PREF_LAST_ACCOUNT, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

        // Populate the account details in the header section
        populateAccountDetails();

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if any account changes took place while paused or stopped
        checkAccounts();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_SELECTED_ACCOUNT, mSelectedAccount);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedItem);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.header_content)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_content:
                toggleAccountView();
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get args
        String blogId = args.getString(LOADER_KEY_BLOG_ID);
        String email = args.getString(LOADER_KEY_EMAIL);

        // Return loader for the specified id
        switch (id) {
            case LOADER_BLOG_NAME:
                // Get title setting
                return new CursorLoader(getActivity(), ContentProvider.createUri(Setting.class,
                        null), null, "blog_id = ? AND key = ?", new String[]{blogId,
                        Setting.Key.TITLE.toString()}, null);
            case LOADER_USER:
                // Get user details
                return new CursorLoader(getActivity(), ContentProvider.createUri(User.class, null),
                        null, "blog_id = ? AND email = ?", new String[]{blogId, email}, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_BLOG_NAME:
                // Validity check
                if (cursor == null || cursor.getCount() == 0) {
                    Log.e(TAG, "Failed to load setting");

                    // Make sure a sync is scheduled to fix the problem
                    SyncHelper.requestSync(mSelectedAccount, getString(R.string.content_authority));
                    return;
                }

                // Create a model from the cursor as it's easier than lots of string manipulation
                cursor.moveToFirst();
                Setting setting = new Setting();
                setting.loadFromCursor(cursor);

                // Set the title/url
                mBlogName.setText(setting.value);
                mBlogUrl.setText(setting.blog.url);
                break;
            case LOADER_USER:
                // Validity check
                if (cursor == null || cursor.getCount() == 0) {
                    Log.e(TAG, "Failed to load user");

                    // Make sure a sync is scheduled to fix the problem
                    SyncHelper.requestSync(mSelectedAccount, getString(R.string.content_authority));
                    return;
                }

                // Create a model from the cursor as it's easier than lots of string manipulation
                cursor.moveToFirst();
                User user = new User();
                user.loadFromCursor(cursor);

                // Show cover image
                if (user.cover != null) {
                    try {
                        String path = ImageUtils.getUrl(user.blog, user.cover);
                        String filename = ImageUtils.getFilename(getActivity(), user.blog, path);
                        File cover = new File(filename);
                        if (cover.exists()) {
                            Picasso.with(getActivity()).load(cover).fit().centerCrop().into(mCover);
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        Log.e(TAG, "Error loading image");
                    }
                } else {
                    mCover.setImageDrawable(null);
                }

                // Show avatar
                if (user.image != null) {
                    try {
                        String path = ImageUtils.getUrl(user.blog, user.image);
                        String filename = ImageUtils.getFilename(getActivity(), user.blog, path);
                        File avatar = new File(filename);
                        if (avatar.exists()) {
                            Picasso.with(getActivity()).load(avatar).fit().into(mAvatar);
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        Log.e(TAG, "Error loading image");
                    }
                } else {
                    mAvatar.setImageResource(R.drawable.ic_action_social_person);
                }

                // Set text values
                mName.setText(user.name);
                mEmail.setText(user.email);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        try {
            Bundle result = future.getResult();
            // TODO
        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            Log.e(TAG, "Add account operation failed: " + e.getMessage());
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param viewId       The android:id of the top level drawer layout
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int viewId, DrawerLayout drawerLayout) {
        mContainerView = getActivity().findViewById(viewId);
        mDrawerLayout = drawerLayout;

        // Resize the header if necessary
        if (mContainerView instanceof ScrimInsetsScrollView) {
            ScrimInsetsScrollView layout = (ScrimInsetsScrollView) mContainerView;
            final int headerOriginalHeight = getResources()
                    .getDimensionPixelSize(R.dimen.navigation_drawer_header_height);
            layout.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    // Update the height of the header
                    ViewGroup.LayoutParams headerParams = mHeader.getLayoutParams();
                    headerParams.height = headerOriginalHeight + insets.top;
                    mHeader.setLayoutParams(headerParams);

                    // Update the top margin of the content
                    ViewGroup.MarginLayoutParams contentParams = (ViewGroup.MarginLayoutParams)
                            mHeaderContent.getLayoutParams();
                    contentParams.topMargin = insets.top;
                    mHeaderContent.setLayoutParams(contentParams);
                }
            });
        }

        // Set status bar colour for Lollipop
        Resources.Theme theme = getActivity().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark});
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(typedArray
                .getResourceId(0, 0)));

        // Set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Setup actionbar button and toggle
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (mAccountListVisible) {
                    toggleAccountView();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                // Make sure keyboard is hidden
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Populate the drawer
        populateDrawerItems();
        populateAccountItems();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mContainerView);
    }

    public void setDrawerIndicatorEnabled(boolean enabled) {
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(enabled);
        }
    }

    public Account getSelectedAccount() {
        return mSelectedAccount;
    }

    private void selectItem(int item) {
        if (mCallbacks != null && mCallbacks.onNavigationDrawerItemSelected(item)) {
            mCurrentSelectedItem = item;
            if (mItemViews != null) {
                for (int i = 0; i < mItemViews.length; i++) {
                    if (mItems.get(i) != ITEM_DIVIDER) {
                        mItemViews[i].setSelected(mItems.get(i) == item);
                        colorView(mItemViews[i], mItems.get(i) == item);
                    }
                }
            }
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mContainerView);
        }
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Load the accounts list and make sure that an account is selected/active
     */
    private void getAccounts(String selectedName) {
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        mAccounts = new ArrayList<>(Arrays.asList(accounts));
        if (mSelectedAccount == null && selectedName != null) {
            for (Account account : mAccounts) {
                if (account.name.equals(selectedName)) {
                    mSelectedAccount = account;
                    break;
                }
            }
        }
        if (mSelectedAccount == null) {
            mSelectedAccount = mAccounts.get(0);
        }
    }

    /**
     * Check to see if accounts have changed and fix any stale data/UI
     */
    private void checkAccounts() {
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        List<Account> currentAcccounts = new ArrayList<>(Arrays.asList(accounts));

        // If no accounts found bump to the splash screen
        if (currentAcccounts.size() == 0) {
            Intent intent  = new Intent(getActivity(), SplashActivity.class);
            startActivity(intent);
            getActivity().finish();
            return;
        }

        // Look for account changes
        Set<Account> existingSet = new HashSet<>();
        existingSet.addAll(mAccounts);
        Set<Account> currentSet = new HashSet<>();
        currentSet.addAll(currentAcccounts);
        if (!currentSet.equals(existingSet)) {
            mAccounts = currentAcccounts;
            if (!mAccounts.contains(mSelectedAccount)) {
                changeAccount(mAccounts.get(0));
            } else {
                populateAccountItems();
            }
        }
    }

    /**
     * Change the active account
     *
     * @param account
     */
    private void changeAccount(Account account) {
        // Change selected account
        mSelectedAccount = account;

        // Update preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putString(PREF_LAST_ACCOUNT, account.name).apply();

        // Update UI
        populateAccountItems();
        populateAccountDetails();
        selectItem(ITEM_POSTS);
    }

    /**
     * Populate account details for the current account
     */
    private void populateAccountDetails() {
        // Get user details
        String blogUrl = mAccountManager.getUserData(mSelectedAccount, KEY_BLOG_URL);
        String email = mAccountManager.getUserData(mSelectedAccount, KEY_EMAIL);
        Blog blog = DatabaseUtils.getBlog(blogUrl, email);

        // If blog record is missing, create it and request a sync so that we can start the loaders
        // and they will load the missing data as it becomes available
        if (blog == null) {
            blog = new Blog();
            blog.url = blogUrl;
            blog.email = email;
            blog.save();

            // Show dialog
            AccountWarningDialog dialog = new AccountWarningDialog();
            dialog.show(getFragmentManager(), "warning");

            // Make sure a sync is scheduled to fix the problem
            SyncHelper.requestSync(mSelectedAccount, getString(R.string.content_authority));
        }

        // Create bundle of loader args
        Bundle args = new Bundle();
        args.putString(LOADER_KEY_BLOG_ID, blog.getId().toString());
        args.putString(LOADER_KEY_EMAIL, email);

        // Load the blog and account data using the content provider so that any changes to the
        // data will be notified
        getLoaderManager().restartLoader(LOADER_BLOG_NAME, args, this);
        getLoaderManager().restartLoader(LOADER_USER, args, this);
    }

    /**
     * Populate the drawer items
     */
    private void populateDrawerItems() {
        // Generate a list of items to show
        mItems.clear();
        mItems = new ArrayList<>();
        mItems.add(ITEM_POSTS);
        mItems.add(ITEM_PAGES);
        mItems.add(ITEM_DIVIDER);
        mItems.add(ITEM_SETTINGS);
        mItems.add(ITEM_ABOUT);

        // Generate views
        mItemList.removeAllViews();
        mItemViews = new View[mItems.size()];
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        int position = 0;
        for (final Integer item : mItems) {
            View view;
            if (item == ITEM_DIVIDER) {
                view = layoutInflater.inflate(R.layout.item_navigation_drawer_divider, mItemList, false);
            } else {
                view = layoutInflater.inflate(R.layout.item_navigation_drawer, mItemList, false);
                ImageView iconView = (ImageView) view.findViewById(R.id.img_icon);
                TextView titleView = (TextView) view.findViewById(R.id.txt_title);
                iconView.setImageResource(ICONS[item]);
                titleView.setText(TITLES[item]);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(item);
                    }
                });
                view.setSelected(mCurrentSelectedItem == item);
                colorView(view, mCurrentSelectedItem == item);
            }
            mItemViews[position] = view;
            mItemList.addView(view);
            position++;
        }
    }

    /**
     * Populate the account items
     */
    private void populateAccountItems() {
        // Add accounts
        mAccountList.removeAllViews();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        for (final Account account : mAccounts) {
            if (account.equals(mSelectedAccount)) {
                continue;
            }
            View view = layoutInflater.inflate(R.layout.item_navigation_drawer_account, mAccountList, false);
            TextView titleView = (TextView) view.findViewById(R.id.txt_title);
            titleView.setText(mAccountManager.getUserData(account, AccountConstants.KEY_EMAIL));
            TextView subtitleView = (TextView) view.findViewById(R.id.txt_subtitle);
            subtitleView.setText(mAccountManager.getUserData(account, AccountConstants.KEY_BLOG_URL));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Change selected account
                    changeAccount(account);
                }
            });
            colorView(view, false);
            mAccountList.addView(view);
        }

        // Add add account view
        View view = layoutInflater.inflate(R.layout.item_navigation_drawer_account_action,
                mAccountList, false);
        ImageView iconView = (ImageView) view.findViewById(R.id.img_icon);
        TextView titleView = (TextView) view.findViewById(R.id.txt_title);
        iconView.setImageResource(R.drawable.ic_action_content_add);
        titleView.setText(R.string.navigation_drawer_add_account);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountManager.addAccount(getString(R.string.account_type), null, null, null,
                        getActivity(), NavigationDrawerFragment.this, null);
            }
        });
        colorView(view, false);
        mAccountList.addView(view);
    }

    /**
     * Set the view colours based on selection state
     *
     * @param view     View to apply colours to
     * @param selected If the view is the selected item
     */
    private void colorView(View view, boolean selected) {
        // Find views
        ImageView iconView = (ImageView) view.findViewById(R.id.img_icon);
        TextView titleView = (TextView) view.findViewById(R.id.txt_title);
        TextView subtitleView = (TextView) view.findViewById(R.id.txt_subtitle);

        // Get colours from theme
        Resources.Theme theme = getActivity().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.colorPrimary,
                R.attr.colorPrimaryDark});
        int colorPrimary = getResources().getColor(typedArray.getResourceId(0, 0));
        int colorPrimaryDark = getResources().getColor(typedArray.getResourceId(1, 0));

        // Colour the icon/title
        if (selected) {
            if (iconView != null) {
                iconView.setColorFilter(colorPrimaryDark);
            }
            if (titleView != null) {
                titleView.setTextColor(colorPrimaryDark);
            }
            if (subtitleView != null) {
                subtitleView.setTextColor(colorPrimaryDark);
            }
        } else {
            if (iconView != null) {
                iconView.setColorFilter(colorPrimary);
            }
            if (titleView != null) {
                titleView.setTextColor(colorPrimary);
            }
            if (subtitleView != null) {
                subtitleView.setTextColor(colorPrimary);
            }
        }
    }

    /**
     * Toggle the account view
     */
    private void toggleAccountView() {
        if (mAccountListVisible) {
            mAccountIndictor.setImageResource(R.drawable.ic_drawer_accounts_expand);
            mAccountList.setVisibility(View.GONE);
            mItemList.setVisibility(View.VISIBLE);
            mAccountListVisible = false;
        } else {
            mAccountIndictor.setImageResource(R.drawable.ic_drawer_accounts_collapse);
            mAccountList.setVisibility(View.VISIBLE);
            mItemList.setVisibility(View.GONE);
            mAccountListVisible = true;
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when an item in the navigation drawer is selected.
         */
        public boolean onNavigationDrawerItemSelected(int id);

    }

}
