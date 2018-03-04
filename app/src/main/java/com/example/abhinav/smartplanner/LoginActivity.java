package com.example.abhinav.smartplanner;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onClick(View view) {
        AutoCompleteTextView emailView = findViewById(R.id.login_email);
        EditText passView = findViewById(R.id.login_password);
        String email = emailView.getText().toString();
        String password = passView.getText().toString();
        if (AccountManager.login(email, password)) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Couldn't sign in!", Toast.LENGTH_SHORT).show();
        }
    }
}
