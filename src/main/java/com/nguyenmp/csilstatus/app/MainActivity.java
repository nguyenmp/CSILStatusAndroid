package com.nguyenmp.csilstatus.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ShowDetailsCallback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Check to see if we are authenticated, if not, show the login activity
        String[] credentials = LoginActivity.getCredentials(this);
        if (credentials[0] == null || credentials[1] == null) LoginActivity.logout(this);

        // If we are launching for the first time, get data
        if (savedInstanceState == null) GetComputersService.refresh(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        assert(position >= 0 && position <= 2);
        switch (position) {
            case 0: fragment = ComputerFragment.newInstance();
                break;
            case 1: fragment = UserFragment.newInstance();
                break;
            case 2: fragment = FriendsFragment.newInstance();
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.who_is_at_csil);
                break;
            case 2:
                mTitle = getString(R.string.computer_usage);
                break;
            case 3:
                mTitle = getString(R.string.friends);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            GetComputersService.refresh(this);
            return true;
        } else if (id == R.id.action_logout) {
            LoginActivity.logout(this);
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void showDetails(Type type, String id) {
        FragmentManager fm = getFragmentManager();
        DialogFragment f = null;

        if (type == Type.Computer) {
            f = ComputerDialogFragment.newInstance(id);
        } else if (type == Type.User) {
            f = UserDialogFragment.newInstance(id);
        }

        if (f != null) f.show(fm, "dialog");
    }

    public MainActivity() {
        super();
    }
}
