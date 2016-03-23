package org.openalpr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
 *
 * date@(19.03.2016) editor@(cameronDz)
 * Added server timeout error listener to the POST response
 *
 * date@(20.03.2016) editor@(cameronDz)
 * Added AlertDialog popups to all errors and server processing where the
 * process might be interrupted, an error may occur, and when username is
 * successfully registered.
 *
 * date@(21.03.2016) editor@(cameronDz)
 * Checked and changed the expected server POST key values, and expected
 * server response key values.
 *
 * date@(23.03.2016) editor@(cameronDz)
 * Removed Volley JSONRequests and replaced with StringRequests. Removed
 * unnecessary methods. Now take expected String response from server,
 * convert to a JSON Object, and extract data from that Object
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

            // make button unclickable to avoid sending multiple registrations
            view.setClickable(false);
            // data sent out to server using Volley HTTP POST. determines if user
            // name is available and sends user to activity according to response
            sendDataToServer(view);
        }
    }

    /**
     * Sends registration data to server
     * @param view used to turn button back on after an error occurs
     */
    private void sendDataToServer(final View view) {
        Log.d(TAG, "sendDataToServer");

        String url = Constants.aws_address;
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        // convert response from server to JSON, send user to new
                        // activity if successful registration, or inform of fail
                        interpretResponse(response, view);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d(TAG, "Error.Response: " + error.getMessage());


                        Log.d(TAG, "Response Error: " + error.getMessage());

                        // check for server timeout error
                        if (error.networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                Log.d(TAG, "Response Error: server timeout");

                                // display pop up to user informing of server timeout
                                String message = "There may be a problem with the " +
                                        "server. Please press Re-Try to reattempt " +
                                        "to register.";
                                String confirm = "Re-Try.";
                                userPopUp(message, confirm, false);
                            }
                        } else {
                            Log.d(TAG, "Error: server problem");

                            // display pop up to user informing of server issue
                            // usual error is no internet access
                            String message = "There may be a problem with your " +
                                    "internet. Please check your connection to " +
                                    "the internet and press Re-Try to reattempt " +
                                    "to register.";
                            String confirm = "Re-Try.";
                            userPopUp(message, confirm, false);
                        }
                        // turn register button back on after error
                        view.setClickable(true);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Log.d(TAG, "getParams");
                Map<String, String> params = new HashMap<>();
                params.put("message_type", "register");
                params.put("user_name", Variables.username);
                params.put("user_password", Variables.password);
                params.put("gcm_user_id", Variables.gcm_user_id);

                return params;
            }
        };
        queue.add(postRequest);
    }

    /**
     * @param sResponse String from server converted to JSON and interpreted
     * @param view used to turn register button back on after error occurs
     * Toast user explaining success/failure, redirects if GUI if success
     */
    private void interpretResponse(String sResponse, View view) {
        Log.d(TAG, "interpretResponse() from server");

        try{
            // convert server response to JSON
            JSONObject jResponse = new JSONObject(sResponse);
            // check for output key, meaning successful registration
            if( jResponse.has("output") ) {
                Log.d(TAG, "interpretResponse() = output");

                // set user id global variables\
                Variables.user_id = (Integer)jResponse.get("user_id");

                // display pop up informing user of successful plate registration
                String message = "You have registered the name: " + Variables.username +
                        ". Press Continue to register a plate.";
                String confirm = "Continue.";
                // on confirmation of popup, sends to next activity
                userPopUp(message, confirm, true);

            // check for error key, meaning server is returning an error
            } else if ( jResponse.has("error") ) {
                Log.d(TAG, "interpretResponse() = error");

                // get error from JSON
                String error = jResponse.get("error").toString();
                // display pop up to user bad plate registration data
                String message = "There was a problem with your username. " +
                        "The error was: " + error + ". " +
                        "Press Re-Try to reattempt user registration.";
                String confirm = "Re-Try.";
                userPopUp(message, confirm, false);

            // server returned some unknown response
            } else {
                Log.d(TAG, "interpretResponse() = unknown response: " + jResponse.toString());

                // display pop up to user bad plate registration data
                String message = "There was a server problem. " +
                        "Press Re-Try to reattempt user registration.";
                String confirm = "Re-Try.";
                userPopUp(message, confirm, false);
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSON get error: " + je);
            je.printStackTrace();

            // display pop up to user informing of error
            String message = "There was an error processing the server response. " +
                    "Sorry for the inconvenience. Press Re-Try to attempt to register again.";
            String confirm = "Re-Try.";
            userPopUp(message, confirm, false);
        }

        // clear username and password global variables
        Variables.username = "";
        Variables.password = "";
        // turn register button back on after an error
        view.setClickable(true);
    }

    /**
     * Create pop up for user to inform about server response or data processing
     * @param message message displayed to user
     * @param confirm acceptance button text
     * @param pass boolean telling whether method should send to next activity or not
     */
    private void userPopUp(String message, String confirm, final boolean pass) {
        Log.d(TAG, "userPopUp");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(false).setPositiveButton(confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "userPopUp : onClick");
                        // if the user registration passed, more to next activity
                        if( pass ) {
                            Log.d(TAG, "userPopUp: pass");
                            // change to plate confirmation activity
                            Intent intent = new Intent(context, ConfirmPlateActivity.class);
                            startActivity(intent);
                        }
                    }
                });
        // display message
        builder.create().show();
    }
}
