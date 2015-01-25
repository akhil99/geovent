package com.buildncode.geovent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    Button signinButton;
    Button signupButton;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupLoginButton();
        username = (EditText)findViewById(R.id.edittext_username);
        password = (EditText)findViewById(R.id.edittext_password);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            finishActivity();
            //currentUser.logOut();
        }
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    private void setupLoginButton(){
        signinButton = (Button)findViewById(R.id.login_button);
        signinButton.setOnClickListener(this);
        signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_button){
            Log.d(TAG, "button pressed");
            logIn();
        }else if(v.getId() == R.id.signup_button){
            Log.d(TAG, "signup button pressed");
            signUp();
        }
    }

    private void logIn(){
        Log.d(TAG, "login");
        ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                Log.d(TAG, "done");
                if (user != null) {
                    // Hooray! The user is logged in.
                    Log.d(TAG, "successfully signed in");
                    finishActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void signUp(){
        Log.d(TAG, "SignUp");
        ParseUser user = new ParseUser();
        user.setUsername(username.getText().toString());
        user.setPassword(password.getText().toString());
        user.setEmail(username.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    Log.d(TAG, "SIGNED UP");
                    finishActivity();
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });
    }

    private void finishActivity() {
        Log.d(TAG, "finish activity");
        //Intent map = new Intent(this, MapActivity.class);
        Intent map = new Intent(this, MapActivity.class);
        startActivity(map);
        finish();
    }
}
