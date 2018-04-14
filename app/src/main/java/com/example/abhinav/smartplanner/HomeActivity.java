package com.example.abhinav.smartplanner;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            OnFragmentInteractionListener{

    private static final int RC_SIGN_IN = 123;

    private DrawerLayout mDrawer = null;
    private Toolbar mToolbar = null;
    private FragmentManager mFManager = null;
    private Resources mRes = null;

    List<AuthUI.IdpConfig> providers = Collections.singletonList(
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRes = getResources();
        setContentView(R.layout.activity_splash);

        App.get().homeActivity = this;
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
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                initializeUI();
            } else {
                Toast.makeText(this, "Sign in failed!", Toast.LENGTH_SHORT).show();
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
        Class fragmentClassEvent = null;
        String fragmentName = null;

        switch (id) {
            case R.id.nav_cal:
                fragmentClassEvent = CalendarFragment.class;
                fragmentName = mRes.getString(R.string.nav_cal);
                break;
            case R.id.nav_tasks:
                fragmentClassEvent = TasksFragment.class;
                fragmentName = mRes.getString(R.string.nav_tasks);
                break;
            case R.id.nav_events:
                fragmentClassEvent = EventsFragment.class;
                fragmentName = mRes.getString(R.string.nav_events);
                break;
            case R.id.nav_db:
                fragmentClassEvent = VAFragment.class;
                fragmentName = mRes.getString(R.string.nav_va);
                break;
            case R.id.nav_logout:
                logout();
                return true;
        }

        try {
            fragment = (Fragment) fragmentClassEvent.newInstance();
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
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void login() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),RC_SIGN_IN);
        } else {
            initializeUI();
        }
    }

    private void initializeUI() {
        setContentView(R.layout.activity_home);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(mRes.getString(R.string.nav_va));
        setSupportActionBar(mToolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

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
}
