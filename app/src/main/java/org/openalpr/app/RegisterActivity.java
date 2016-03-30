package org.openalpr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Anthony Brignano on 2/19/16.
 *
 * date@(23.03.2016) editor@(cameronDz)
 * Removed all methods and calls within class dealing with sending data to
 * the server, and replaced with one call in the Register listener that
 * sends all HTTP POST and server response interpretation to the HTTPService
 * class. Class handles all errors and redirects depending. Also removed all
 * old date/editor comments pertaining to server interaction, since no longer
 * relevant to this class. Also removed all GCM related functions for now.
 */

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "RegisterActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // set context, used in sending data to server
        context = this;
    }

    /**
     * FORMAT: "variable_name - function"
     *
     * u - EditText variable for text input in username field
     * p - EditText variable for text input in password field
     * cp - EditText variable for text input in confirm_password field
     * username - String variable for text in username field
     * password - String variable for text in password field
     * confirm_password - String variable for text in password field
     */
    public void Register(View view) {
        Log.d(TAG, "Register Button Pressed");
        // get user entered name and password from view
        EditText u = (EditText)findViewById(R.id.username);
        EditText p = (EditText)findViewById(R.id.password);
        EditText cp = (EditText)findViewById(R.id.confirm_password);
        String username = u.getText().toString();
        String password = p.getText().toString();
        String confirm_password = cp.getText().toString();

        // password check
        Boolean passCheck = false;
        if(password.equals(confirm_password)){
            Log.d(TAG, "Password check: Pass");
            passCheck = true;
        } else {
            Log.d(TAG, "Password check: Fail");
            // reset views password
            p.setText("", TextView.BufferType.EDITABLE);
            cp.setText("", TextView.BufferType.EDITABLE);
            int duration = Toast.LENGTH_SHORT;
            // displays message to user if passwords don't match
            String message = "Passwords do not match.";
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }

        // send data to server if password check passes
        if(passCheck){
            Log.d(TAG, "passCheck: true");

            // save user name and password to be verified for global variables
            Variables.username = username;
            Variables.password = password;

            // make button unclickable to avoid sending multiple registrations
            view.setClickable(false);
            // data sent out to server using Volley HTTP POST. determines if user
            // name is available and sends user to activity according to response
            HTTPService.sendData(context, view, 1);
        }
    }
}
