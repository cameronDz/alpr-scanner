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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Anthony Brignano on 2/17/16.
 *
 * LoginActivity: Allows users to login to an existing account
 * (associated view activity_login)
 *
 * - redirectToRegister(View): redirects user to RegisterActivity.java
 * (activity_register.xml) on click
 * - Login(View): parses user credentials entered into the views TextFields
 * - onActivityResult(int, int, Intent):
 * - onCreate(Bundle)
 *
 * date@(23.03.2016) editor@(cameronDz)
 * Removed all methods and calls within class dealing with sending data to
 * the server, and replaced with one call in the Login listener that
 * sends all HTTP POST and server response interpretation to the HTTPService
 * class. Class handles all errors and redirects depending. Also removed all
 * old date/editor comments pertaining to server interaction, since no longer
 * relevant to this class. Also removed all GCM related functions for now.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context context;
    private String testJSON = convertJson("0101", "December 21, 2012", "0.1", "0.2", "1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // set context, used in sending data to server
        context = this;
        GoogleCloudMessaging.getInstance(context);

        // TESTING PURPOSE
        storeMessage(testJSON, context);

        // check for gcm token, get one if there isn't one
        if(Variables.gcm_user_id.equals("") ) {
            // get an instance id
            Variables.gcm_inst_id = InstanceID.getInstance(context).getId();
            // off main thread, obtain token to be user_gcm_id
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    Log.v(TAG, "doInBackGround");
                    String msg = "Failed to Obtain";
                    String authorizedEntity = Variables.SENDER_ID;
                    String scope = "GCM";
                    try {
                        // attempt to get token
                        Variables.gcm_user_id = InstanceID.getInstance(context).getToken(authorizedEntity, scope);
                        msg = Variables.gcm_user_id;
                        Log.v(TAG, "gcm_user_id: " + Variables.gcm_user_id);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v(TAG, "GCM IOException: " + e);
                    }
                    return msg;
                }

                // Log the msg return from Task
                @Override
                protected void onPostExecute(String msg) {
                    Log.d(TAG, "onPostExecute: " + msg);
                }
            }.execute(null, null, null);
        }
    }

    /**
     * FORMAT: "variable_name - function"
     * <p/>
     * u - EditText variable for text input in username field
     * p - EditText variable for text input in password field
     * username - String variable for text in username field
     * password - String variable for text in password field
     *
     * @param view current view of app
     */
    public void Login(View view) {
        Log.d(TAG, "Login Button Pressed");

        // get user input from view
        EditText u = (EditText) findViewById(R.id.username);
        EditText p = (EditText) findViewById(R.id.password);
        String username = u.getText().toString();
        String password = p.getText().toString();
        // set global username
        Variables.username = username;
        Variables.password = password;

        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        // make button unclickable to avoid sending multiple registrations
        view.setClickable(false);
        // data sent out to server using Volley HTTP POST. determines if
        // username and password sent to server match on the database
        HTTPService.sendData(context, view, 3);
    }

    /**
     * Register button is pressed, sends user to register activity
     * @param view button to change to register activity
     */
    public void redirectToRegister(View view) {
        Log.d(TAG, "Register Button Pressed");
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * TODO ask Anthony what this does
     * @param requestCode TODO ask Anthony
     * @param resultCode  TODO ask Anthony
     * @param intent      TODO ask Anthony
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Intent scanIntent = new Intent(this, ScanPlate.class);
        String platePath = intent.getStringExtra("picture");

        scanIntent.putExtra("platepicture", platePath);

        Log.d(TAG, "Starting ScanPlate.class");
        Log.d(TAG, "Image file path: " + platePath);

        startActivity(scanIntent);
    }
    /**
     * TESTING!!!!
     * Will be how a message is broken down and saved on a device
     * @param sMessage string representation of JSON object of message
     * @param context context in which the file is being written
     */
    protected void storeMessage(String sMessage, Context context) {
        Log.d(TAG, "receiveMessage: " + sMessage);

        // add new line character to the end of the message
        sMessage = sMessage + "\n";
        FileOutputStream file;
        // open file to be written to
        try {
            file = context.openFileOutput(Variables.MESSAGE_FILE, Context.MODE_APPEND);
            // write new message to file
            try {
                file.write( sMessage.getBytes() );
                file.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e);
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Convert the GCM Bundle data into a string representation of a
     * JSON object for storing purposes
     * @param mid message id
     * @param timestamp timestamp of message
     * @param gpsLon longitude coordinates of incident
     * @param gpsLat latitude coordinates of incident
     * @param message user message
     * @return JSON object of data
     */
    protected String convertJson(String mid, String timestamp, String gpsLon, String gpsLat, String message) {
        Log.d(TAG, "convertJSON");
        String json = "{\"mid\":\""         + mid       + "\"," +
                "\"timestamp\":\""   + timestamp + "\"," +
                "\"gps_lon\":\""     + gpsLon    + "\"," +
                "\"gps_lat\":\""     + gpsLat    + "\"," +
                "\"message\":\""     + message   + "\"}";
        // read : 0 or 1
        // TODO add boolean for read/not read
        Log.d(TAG,"convertJSON result: " + json);
        return json;
    }

}
