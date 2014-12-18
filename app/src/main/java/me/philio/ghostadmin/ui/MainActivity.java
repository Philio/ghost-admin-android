package me.philio.ghostadmin.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.philio.ghostadmin.R;

public class MainActivity extends BaseActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        PostsFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

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
    public void onNavigationDrawerItemSelected(int item) {
        Fragment fragment = null;
        switch (item) {
            case NavigationDrawerFragment.ITEM_POSTS:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_POSTS | PostsFragment.SHOW_DRAFTS);
                break;
            case NavigationDrawerFragment.ITEM_PAGES:
                fragment = PostsFragment.newInstance(mNavigationDrawerFragment.getSelectedAccount(),
                        PostsFragment.SHOW_PAGES | PostsFragment.SHOW_DRAFTS);
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onListItemClick(long id) {

    }

}
