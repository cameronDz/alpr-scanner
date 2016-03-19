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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anthony Brignano on 2/19/16.
 *
 * date@(07.03.2016) editor@(cameronDz)
 * Sent username and password variables to global Variable static class.
 *
 * date@(17.03.2016) editor@(cameronDz)
 * Added JSON formatting class for registering user, removed GCM upstream,
 * added framework for Volley HTTP POST. Put logs in methods. Volley needs
 * to be tested and tweaked.
 *
 * date@(18.03.2016) editor@(cameronDz)
 * Added check for empty JSON being sent to server.
 */

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "RegisterActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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

            // data sent out to server using Volley HTTP POST. determines if user
            // name is available and sends user to activity according to response
            sendDataToServer();
        }
    }

    /**
     * Sends registration data to server
     */
    private void sendDataToServer() {
        Log.d(TAG, "sendDataToServer");

        // requests queue to be sent to server
        RequestQueue queue = Volley.newRequestQueue(this);
        // JSONObject to be sent to server
        JSONObject json = formatJSONRegister();

        // checks to make sure JSON has data in it
        if ( !(json.toString().equals("{}")) ) {
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
                                    Log.d(TAG, "onErrorResponse: " + error.getMessage());

                                    // TODO check for server timeout error
                                    // TODO create server response error Toast
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
     * @param response JSONObject returned from server register information
     * Toast user explaining success/failure, redirects if GUI if success
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse() from server");

        // attempt to breakdown JSON response
        try{
            String registration = response.get("register").toString();
            // TODO make make sure server is returning expected JSON "register"
            if( registration.equals("success") ) {
                Log.d(TAG, "interpretResponse: success");

                // set user id global variables\
                Variables.user_id = (Integer)response.get("user_id");
                // TODO make make sure server is returning expected JSON "user_id"

                // TODO add a toast or popup informing user of success

                // change to plate confirmation activity
                Intent intent = new Intent(this, ConfirmPlateActivity.class);
                startActivity(intent);
            } else if( registration.equals("fail") ) {
                Log.d(TAG, "interpretResponse: failed");
                // clear username and password global variables
                Variables.username = "";
                Variables.password = "";

                // TODO put Toast here informing user why failure occurred
                // TODO add failure reason logic from JSON and put in Toast

                // TODO tell user to reattempt to register, or restart activity
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSON get error: " + je);
            je.printStackTrace();
        }

        // TODO add error Toast
    }

    /**
     * @return string form of JSON object to be sent and register a new
     *          user, on JSONException error, returns empty object
     */
    private JSONObject formatJSONRegister() {
        Log.d(TAG, "formatJSONRegister data to send to server");

        JSONObject reg = new JSONObject();
        try {
            // server is set to recognize "messageType" in JSON object and
            // process data accordingly
            reg.put("messageType", "register_user");
            reg.put("username", Variables.username);
            reg.put("password", Variables.password);
            reg.put("gcm_user_id", Variables.gcm_user_id);
            // TODO TEST that this is how server is expecting JSON

        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSON format error" + je);
            reg = new JSONObject();
        }
        Log.d(TAG,"formatJSONRegister: " + reg.toString() );
        return reg;
    }
}
