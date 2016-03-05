package org.openalpr.app;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

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
        context = getApplicationContext();

        // Register an InstanceID
        Log.v("GCM_REGISTER", "BEFORE");
        Constants.INST_ID =  InstanceID.getInstance(context).getId();
        Log.v("GCM_REGISTER", "AFTER " + Constants.INST_ID);


        // Get token
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "FAIL";
                Log.v("GCM_TOKE", "BEFORE");
                String authorizedEntity = Constants.SENDER_ID;
                String scope = "GCM";
                try {
                    Log.v("GCM_TOKE", "BEFORE_IN_TRY");
                    Constants.REG_TOKEN = InstanceID.getInstance(context).getToken(authorizedEntity, scope);
                    msg = Constants.REG_TOKEN;
                    Log.v("GCM_TOKE", "AFTER_IN_TRY");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v("GCM_TOKE", "FAIL FAIL FAIL FAIL");
                    Log.v("GCM_TOKE", "FAIL FAIL FAIL FAIL");
                    Log.v("GCM_TOKE", "FAIL FAIL FAIL FAIL");
                }
                Log.v("GCM_TOKE", "AFTER");

                return msg;
            }



            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);


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


        //gcm sends hi message to server
        Constants.gcm = GoogleCloudMessaging.getInstance(this);
        Log.v("GCM_PRINT", Constants.gcm.toString());
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "Sent message";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "SAY_HELLO");
                    data.putString("my_action2", "SAY_HELLO");
                    data.putString("my_action3", "SAY_HELLO");
                    data.putString("my_action4", "SAY_HELLO");
                    data.putString("my_action5", "SAY_HELLO");
                    String id = Integer.toString(Constants.MSG_ID) + "alpr";
                    Constants.MSG_ID++;
                    Log.v("GCM_SEND", "BEFORE");
                    Log.v("GCM_SEND", "BEFORE_TOKEN: " + Constants.REG_TOKEN);
                    Log.v("GCM_SEND", "BEFORE_PROJECT_ID: " + Constants.PROJECT_ID);
                    Constants.gcm.send(Constants.PROJECT_ID + "@gcm.googleapis.com", id, data);
                    Log.v("GCM_SEND", "AFTER_data: " + data.toString());
                    Log.v("GCM_SEND", "AFTER");
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.v("GCM_SEND", "Error");
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);


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