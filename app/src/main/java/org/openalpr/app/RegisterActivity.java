package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Anthony Brignano on 2/19/16.
 */

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "RegisterActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
    }

    public void Register(View view) {
        EditText u = (EditText)findViewById(R.id.username);
        EditText p = (EditText)findViewById(R.id.password);
        EditText cp = (EditText)findViewById(R.id.confirm_password);
        String username = u.getText().toString();
        String password = p.getText().toString();
        String confirm_password = cp.getText().toString();
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);
        Log.d(TAG, "Confirm Password: " + confirm_password);

        // Logic needs to be added to this variable based on the database interaction(s)
        Boolean registrationComplete = false;

        /**
         * FORMAT: "variable_name - function"
         *
         * u - EditText variable for text input in username field
         * p - EditText variable for text input in password field
         * cp - EditText variable for text input in confirm_password field
         * username - String variable for text in username field
         * password - String variable for text in password field
         * confirm_password - String variable for text in password field
         *
         * */

        if(password.equals(confirm_password)){
            registrationComplete = true;
        }
        else{
            // displays message to user
            String message = "Passwords do not match.";
            p.setText("", TextView.BufferType.EDITABLE);
            cp.setText("", TextView.BufferType.EDITABLE);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }

        if(registrationComplete){
            Intent intent = new Intent(this, ConfirmPlateActivity.class);
            startActivity(intent);
        }
    }
}