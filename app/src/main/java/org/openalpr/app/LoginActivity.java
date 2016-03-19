package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Anthony Brignano on 2/17/16.
 * <p/>
 * LoginActivity: Allows users to login to an existing account
 * (associated view activity_login)
 * <p/>
 * - redirectToRegister(View): redirects user to RegisterActivity.java
 *      (activity_register.xml) on click
 * - Login(View): parses user credentials entered into the views TextFields
 * - onActivityResult(int, int, Intent):
 * - onCreate(Bundle)
 *
 * date@(17.03.2016) @editor(cameronDz)
 * Set onCreate method to get an InstanceID and token for GCM every
 * time the app starts. Set Volley HTTP POST to attempt login with
 * response logic set to check if server JSON response, and send user
 * to the appropriate view/message.
 *
 * date@(18.03.2016) @editor(cameronDz)
 * Added check for empty JSON being sent to server.
 *
 * date@(19.03.2016) editor@(cameronDz)
 * Added server timeout error listener to the POST response
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context context;
    protected GoogleCloudMessaging gcm = null;
    protected InstanceID iid = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // method uses AsyncTask to get a GCM registration token
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();

        // GCM InstanceID is refreshed and saved globally in app
        iid = InstanceID.getInstance(context);
        gcm = GoogleCloudMessaging.getInstance(context);
        Constants.INST_ID = iid.getId();

        // GCM API token is refreshed and saved globally
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d(TAG, "doInBackground: GCM API");

                String msg = "";
                // attempts to get a Registration token for GCM
                try {
                    msg = InstanceID.getInstance(context).getToken(Constants.PROJECT_ID, "GCM");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(TAG, "IOException: " + e);
                }

                // returns the token in string form, null if no token was received
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.v(TAG, "onPostExecute: GCM API");
                // save token in global variables
                Constants.REG_TOKEN = msg;
            }
        }.execute(null, null, null);

        // TODO check if this is needed for Instance ID or for GCM upstream
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * FORMAT: "variable_name - function"
     *
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
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        // username and password sent to server
        attemptLogin(username, password);
    }

    /**
     * Username and password sent out to the server,
     * @param username attempted username sign in
     * @param password attempted password sign in
     */
    private void attemptLogin(String username, String password) {
        Log.d(TAG, "attemptLogin");

        // requests queue to be sent to server
        RequestQueue queue = Volley.newRequestQueue(this);
        // JSONObject to be sent to server
        JSONObject json = formatJSONLogin(username, password);

        // check to make sure json variable is not empty
        if( !(json.toString().equals("{}")) ) {
            // new request to be sent out to server
            Log.d(TAG, "create and send JSON POST request");
            JsonObjectRequest jsonRequest = new JsonObjectRequest
                    (Request.Method.POST, Constants.aws_address, json,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "onResponse: " + response.toString());
                                    // break down JSON response from server, send user to new
                                    // activity if successful registration, or inform of fail
                                    interpretResponse(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error: " + error.getMessage());

                                    // check for server timeout error
                                    if( error.networkResponse == null ) {
                                        if( error.getClass().equals(TimeoutError.class) ) {
                                            Log.d(TAG, "Error: server timeout");

                                            // TODO create server response error Toast
                                        }
                                    } else {
                                        Log.d(TAG, "Error: server problem");

                                        // TODO error Toast
                                    }
                                }
                            });

            // new request added to queue
            queue.add(jsonRequest);
        } else {
            Log.d(TAG, "json variable empty error");

            // TODO create empty JSON error Toast
        }
    }

    /**
     * Takes JSON response from server and decides whether user is
     * authentic or not
     * @param response JSON response from server
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse() form server");

        // attempt to breakdown JSON response
        try{
            // get login status from JSON object
            String login = response.get("login").toString();
            // TODO TEST make sure server is returning expected JSON "login"
            if( login.equals("success") ) {
                Log.d(TAG, "interpretResponse: success");

                // set global username
                Variables.username = response.get("username").toString();
                // TODO TEST make sure server is returning expected JSON "username"

                // TODO add a toast or popup informing user of success

                // send user to home activity
                Intent intent = new Intent(this, ConfirmPlateActivity.class);
                startActivity(intent);
            } else {
                // assume "login : failed"
                Log.d(TAG, "interpretResponse: failed");

                // TODO put Toast here informing user of failure

                // TODO tell user to reattempt login, or restart activity
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSON get error: " + je);
            je.printStackTrace();

            // TODO add error Toast
        }
    }

    /**
     * JSON object prepared to be sent to server for login
     * @param username username to be put into JSON
     * @param password password to be put into JSON
     * @return a JSON object to be sent to server
     */
    private JSONObject formatJSONLogin(String username, String password) {
        Log.d(TAG, "formatJSONLogin()");
        JSONObject json = new JSONObject();

        // attempt to put user login info, and gcm token into JSON
        try {
            json.put("messageType","login");
            json.put("username", username);
            json.put("password", password);
            json.put("gcm_user_id", Variables.gcm_user_id);
            // TODO TEST that this is how server is expecting JSON
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);

            // json is returned as an empty object if error occurs
            json = new JSONObject();
        }
        Log.d(TAG, "formatJSONLogin result: " + json.toString() );
        return json;
    }

    /**
     * Register button is pressed, sends user to register activity
     * @param view current view
     */
    public void redirectToRegister(View view) {
        Log.d(TAG, "Register Button Pressed");
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * TODO ask Anthony what this does
     * @param requestCode TODO ask Anthony
     * @param resultCode TODO ask Anthony
     * @param intent TODO ask Anthony
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

    @Override
    public void onStart() {
        super.onStart();

        // TODO see if this is needed for GCM Instance ID or upstream messaging
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.openalpr.app/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO see if this is needed for GCM Instance ID or upstream messaging
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.openalpr.app/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
