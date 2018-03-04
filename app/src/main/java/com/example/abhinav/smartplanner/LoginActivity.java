package com.example.abhinav.smartplanner;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton mSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mSignInButton = findViewById(R.id.login_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });
    }

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
//
//    private void googleSignOut() {
//        mGoogleSignInClient.signOut();
//    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            loggedIn(account);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            loggedIn(account);
        } catch (ApiException e) {
            Log.w("Google Sign In", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void loggedIn(GoogleSignInAccount account) {
        AccountManager.getInstance().login(account.getEmail(), account.getDisplayName(), account.getPhotoUrl());
        setResult(RESULT_OK);
        finish();
    }
}
