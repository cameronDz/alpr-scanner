package org.openalpr.app;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Anthony Brignano on 2/17/16.
 *
 * LoginActivity: Allows users to login to an existing account
 *      (associated view activity_login)
 *
 *      - redirectToRegister(View): redirects user to RegisterActivity.java (activity_register.xml) on click
 *      - Login(View): parses user credentials entered into the views TextFields
 *      - onActivityResult(int, int, Intent):
 *      - onCreate(Bundle)
 *
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

    }

    public void Login(View view) {
        /**
         * FORMAT: "variable_name - function"
         *
         * u - EditText variable for text input in username field
         * p - EditText variable for text input in password field
         * username - String variable for text in username field
         * password - String variable for text in password field
         *
         * */

        EditText u = (EditText)findViewById(R.id.username);
        EditText p = (EditText)findViewById(R.id.password);
        String username = u.getText().toString();
        String password = p.getText().toString();
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        // Logic needs to be added to this variable based on the database interaction(s)
        Boolean loginSuccess = true;

        if(loginSuccess){
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
        }
    }

    public void redirectToRegister(View view) {
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }



    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Intent scanIntent = new Intent(this, ScanPlate.class);

        String platePath = intent.getStringExtra("picture");

        scanIntent.putExtra("platepicture", platePath);

        Log.d(TAG, "Starting ScanPlate.class");
        Log.d(TAG, "Image file path: " + platePath);

        startActivity(scanIntent);

    }
}