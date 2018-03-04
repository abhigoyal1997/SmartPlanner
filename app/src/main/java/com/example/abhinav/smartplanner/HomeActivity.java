package com.example.abhinav.smartplanner;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            OnFragmentInteractionListener{

    private DrawerLayout mDrawer = null;
    private Toolbar mToolbar = null;
    private FragmentManager mFManager = null;
    private Resources mRes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mRes = getResources();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mRes.getString(R.string.nav_dashboard));
        setSupportActionBar(mToolbar);

        mFManager = getSupportFragmentManager();
        mFManager.beginTransaction().replace(R.id.flContent, DashboardFragment.newInstance()).commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Assistant to be implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        Class fragmentClass = null;
        String fragmentName = null;

        switch (id) {
            case R.id.nav_cal:
                fragmentClass = CalendarFragment.class;
                fragmentName = mRes.getString(R.string.nav_cal);
                break;
            case R.id.nav_tasks:
                fragmentClass = TasksFragment.class;
                fragmentName = mRes.getString(R.string.nav_tasks);
                break;
            case R.id.nav_eval:
                fragmentClass = EvalFragment.class;
                fragmentName = mRes.getString(R.string.nav_eval);
                break;
            case R.id.nav_schedule:
                fragmentClass = ScheduleFragment.class;
                fragmentName = mRes.getString(R.string.nav_schedule);
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                fragmentName = mRes.getString(R.string.nav_settings);
                break;
            case R.id.nav_db:
                fragmentClass = DashboardFragment.class;
                fragmentName = mRes.getString(R.string.nav_dashboard);
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        mToolbar.setTitle(fragmentName);
        item.setChecked(true);

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
