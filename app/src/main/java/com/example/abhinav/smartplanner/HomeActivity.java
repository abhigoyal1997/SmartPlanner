package com.example.abhinav.smartplanner;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            OnFragmentInteractionListener{

    private static final int LOGIN_REQUEST = 99;
    private static final int LOGOUT_REQUEST = 98;
    private DrawerLayout mDrawer = null;
    private Toolbar mToolbar = null;
    private FragmentManager mFManager = null;
    private Resources mRes = null;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mRes = getResources();
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(mRes.getString(R.string.nav_va));
        setSupportActionBar(mToolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        login();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AccountManager.LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                initializeUI();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mDrawer.closeDrawer(GravityCompat.START);

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
                fragmentClass = VAFragment.class;
                fragmentName = mRes.getString(R.string.nav_va);
                break;
            case R.id.nav_logout:
                logout();
                return true;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        mToolbar.setTitle(fragmentName);
        item.setChecked(true);

        return true;
    }

    private void logout() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, "Not connected to internet!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, LoginActivity.class);
        i.setAction(LoginActivity.ACTION_LOGOUT);
        startActivityForResult(i, LOGOUT_REQUEST);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void login() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, LOGIN_REQUEST);
        } else {
            initializeUI();
        }
    }

    private void initializeUI() {
        View navHeaderView = mNavigationView.getHeaderView(0);
        ImageView navImageView = navHeaderView.findViewById(R.id.nav_header_image);
        TextView navNameView = navHeaderView.findViewById(R.id.nav_header_name);
        TextView navEmailView = navHeaderView.findViewById(R.id.nav_header_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            navNameView.setText(user.getDisplayName());
            navEmailView.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Glide.with(getApplicationContext()).load(user.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(navImageView);
            }
        }

        mFManager = getSupportFragmentManager();
        mFManager.beginTransaction().replace(R.id.flContent, VAFragment.newInstance()).commit();
    }

    public void handleTask(JSONObject task) {
        try {
            Toast.makeText(this, task.getString("task"), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
