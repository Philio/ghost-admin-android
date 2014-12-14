package me.philio.ghostadmin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghostadmin.R;
import me.philio.ghostadmin.model.Setting;
import me.philio.ghostadmin.model.User;
import me.philio.ghostadmin.ui.widget.BezelImageView;
import me.philio.ghostadmin.ui.widget.ScrimInsetsScrollView;
import me.philio.ghostadmin.util.ImageUtils;

import static me.philio.ghostadmin.account.AccountConstants.KEY_BLOG_ID;
import static me.philio.ghostadmin.account.AccountConstants.KEY_EMAIL;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Logging tag
     */
    private static final String TAG = NavigationDrawerFragment.class.getName();

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
     * Loaders
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
     * Items in the drawer
     */
    List<Integer> mItems = new ArrayList<>();

    /**
     * Views in the drawer list
     */
    private View[] mItemViews;

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
    @InjectView(R.id.text_name)
    TextView mName;
    @InjectView(R.id.text_email)
    TextView mEmail;
    @InjectView(R.id.text_blog_title)
    TextView mBlogName;
    @InjectView(R.id.text_blog_url)
    TextView mBlogUrl;
    @InjectView(R.id.item_list)
    ViewGroup mItemList;

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
            mCurrentSelectedItem = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Get accounts and set active account
        getAccounts();

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedItem);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

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
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mContainerView);
    }

    private void selectItem(int item) {
        mCurrentSelectedItem = item;
        if (mItemViews != null) {
            for (int i = 0; i < mItemViews.length; i++) {
                if (mItems.get(i) != ITEM_DIVIDER) {
                    mItemViews[i].setSelected(mItems.get(i) == item);
                    colorView(mItemViews[i], mItems.get(i) == item);
                }
            }
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(item);
        }
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Load the accounts list and make sure that an account is selected/active
     */
    private void getAccounts() {
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        mAccounts = new ArrayList<>(Arrays.asList(accounts));
        if (mSelectedAccount == null) {
            mSelectedAccount = mAccounts.get(0);
        }
    }

    /**
     * Populate account details for the current account
     */
    private void populateAccountDetails() {
        // Get user details
        String blogId = mAccountManager.getUserData(mSelectedAccount, KEY_BLOG_ID);
        String email = mAccountManager.getUserData(mSelectedAccount, KEY_EMAIL);

        // Create bundle of loader args
        Bundle args = new Bundle();
        args.putString(LOADER_KEY_BLOG_ID, blogId);
        args.putString(LOADER_KEY_EMAIL, email);

        // Load accounts and load data from database
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
        mItems.add(ITEM_DIVIDER);
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
                TextView titleView = (TextView) view.findViewById(R.id.text_title);
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
     * Set the view colours based on selection state
     *
     * @param view     View to apply colours to
     * @param selected If the view is the selected item
     */
    private void colorView(View view, boolean selected) {
        // Find views
        ImageView iconView = (ImageView) view.findViewById(R.id.img_icon);
        TextView titleView = (TextView) view.findViewById(R.id.text_title);

        // Get colours from theme
        Resources.Theme theme = getActivity().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.colorPrimary,
                R.attr.colorPrimaryDark});
        int colorPrimary = getResources().getColor(typedArray.getResourceId(0, 0));
        int colorPrimaryDark = getResources().getColor(typedArray.getResourceId(1, 0));

        // Colour the icon/title
        if (selected) {
            iconView.setColorFilter(colorPrimaryDark);
            titleView.setTextColor(colorPrimaryDark);
        } else {
            iconView.setColorFilter(colorPrimary);
            titleView.setTextColor(colorPrimary);
        }
    }

    /**
     * Populate the account items
     */
    private void populateAccountItems() {

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
                        null), null, "blog_id = ? AND key = ?", new String[]{blogId, Setting.Key.TITLE.toString()}, null);
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
                    return;
                }

                // Create a model from the cursor as it's easier than lots of string manipulation
                cursor.moveToFirst();
                User user = new User();
                user.loadFromCursor(cursor);

                // Show cover image
                if (user.cover != null) {
                    try {
                        String path = ImageUtils.cleanPath(user.cover);
                        String filename = ImageUtils.getFilename(getActivity(), user.blog, path);
                        File cover = new File(filename);
                        if (cover.exists()) {
                            Picasso.with(getActivity()).load(cover).fit().centerCrop().into(mCover);
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        Log.e(TAG, "Error loading image");
                    }
                }

                // Show avatar
                if (user.image != null) {
                    try {
                        String path = ImageUtils.cleanPath(user.image);
                        String filename = ImageUtils.getFilename(getActivity(), user.blog, path);
                        File avatar = new File(filename);
                        if (avatar.exists()) {
                            Picasso.with(getActivity()).load(avatar).fit().into(mAvatar);
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        Log.e(TAG, "Error loading image");
                    }
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

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int id);

    }

}
