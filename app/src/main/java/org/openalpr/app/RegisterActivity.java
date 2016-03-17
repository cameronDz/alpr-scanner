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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anthony Brignano on 2/19/16.
 *
 * version@(7.3.2016) editor@(cameronDz)
 * Sent username and password variables to global Variable static class.
 *
 * version@(17.3.2016) editor(cameronDz)
 * Added JSON formatting class for registering user, removed GCM upstream,
 * added framework for Volley HTTP POST. Volley needs to be tested and tweaked.
 */

public class RegisterActivity extends AppCompatActivity {

    protected GoogleCloudMessaging gcm = null;
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
            // save user name and password to be verified for global variables
            Variables.username = username;
            Variables.password = password;

            // TODO TEST to make sure connection is being made
            // data sent out to server using Volley HTTP POST. determines if user
            // name is available and sends user to activity according to response
            sendDataToSever();
        }
    }

    /**
     * Sends registration data to server
     */
    private void sendDataToSever() {
        // requests queue to be sent to server
        RequestQueue queue = Volley.newRequestQueue(this);
        // Server address and JSONObject to be sent
        // TODO get AWS address, make sure hard coding it is 'safe'
        String address = "AWS.SERVER.ADDRESS";
        JSONObject json = formatJSONRegister(Variables.username, Variables.password);

        // TODO add a check to make sure json variable is not NULL
        // new request to be sent out to server
        JsonObjectRequest jsonRequest = new JsonObjectRequest
            (Request.Method.POST, address, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // break down JSON response from server, send user to new
                        // activity if successful registration, or inform of fail
                        if( interpretResponse(response) ) {
                            success();
                        } else {
                            fail();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO create error listener
                    }
                });

        // new request added to queue
        queue.add(jsonRequest);
    }

    /**
     * @param response JSONObject returned from server register information
     * @return boolean saying with registration was successful or failed
     */
    private boolean interpretResponse(JSONObject response) {
        // TODO make sure server is returning expected JSON (ask Connor or Matt)
        // attempt to breakdown JSON response
        try{
            if( response.get("register").equals("success") ) {
                return true;
            } else if( response.get("register").equals("fail") ) {
                return false;
            }
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSON get error: " + je);
        }

        // returns false if unable to read JSON
        return false;
    }

    /**
     * Response to user if server response to registration was not successful
     */
    private void fail() {
        // TODO add toast or popup informing user of fail to register
        // TODO tell user to reattempt to register, or restart activity
    }

    /**
     * If server responses is successful registering new user name,
     * sends user to register their plate.
     */
    private void success() {
        // TODO add a toast or popup informing user of success

        Intent intent = new Intent(this, ConfirmPlateActivity.class);
        startActivity(intent);
    }

    /**
     * @param username username a user is attempting to register
     * @param password unencrypted password as user wants tied to name
     *
     * @return string form of JSON object to be sent and register a new
     *          user, on JSONException error, returns NULL; if NULL is
     *          returned, it should be flagged in code calling method
     */
    private JSONObject formatJSONRegister(String username, String password) {
        JSONObject reg = new JSONObject();
        try {
            // server is set to recognize "messageType" in JSON object and
            // process data accordingly
            reg.put("messageType", "register_user");
            reg.put("username", username);
            reg.put("password", password);

            // TODO may need to add a GCM API key to this

            return reg;
        } catch (JSONException je) {
            Log.d(TAG, "JSON format error");
        }
        return null;
    }
}
