package org.openalpr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

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
 * Created by cameronDz on 23.03.2016
 * Uses Volley to send HTTP POST requests to the server, and interpret responses. Manages
 * HTTP POSTs to server interactions in several activities. Used in RegisterActivity,
 * ConfirmPlateActivity, LoginActivity, and MessageSendActivity.
 */
public class HTTPService {
    // put activity in TAG for easier Log searching
    private static String TAG = "HTTPService(Activity)";

    // integers representing the associated activities
    protected static final int REGISTER = 1;
    protected static final int PLATE = 2;
    protected static final int LOGIN = 3;
    protected static final int MESSAGE = 4;
    protected static final int READ = 5;
    protected static final int REPLY = 6;

    /**
     * Used to send data from the app to the AWS server from several different activities.
     * @param context Context of the activity data is being sent to the server from
     * @param view button pressed to send data to the server
     * @param activity integer representation of the activity data is sent from
     */
    protected static void sendData(final Context context, final View view, final int activity) {
        Log.d(TAG, "sendData");

        String url = Variables.AWS_ADDRESS;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        // convert response from server to JSON
                        interpretResponse(response, context, view, activity);
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: " + error.getMessage());

                            // popup Strings for user
                            String message = "", confirm = "Re-Try.";
                            // check for server timeout error
                            if (error.networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    Log.d(TAG, "Response Error: server timeout");
                                    // set message to user
                                    message = "There may be a problem with the " +
                                            "server. Press Re-Try to reattempt to " +
                                            actString(activity) + ".";
                                }
                            } else {
                                Log.d(TAG, "Error: internet connection problem");
                                // set message to user
                                message = "There may be a problem with your " +
                                        "internet. Please check your connection to " +
                                        "the internet and press Re-Try to reattempt " +
                                        "to " + actString(activity) + ".";
                            }
                            //display popup to user
                            userPopUp(message, confirm, context, activity, false);
                            // turn button back on after error
                            view.setClickable(true);
                        }
                    }
                ) {
            @Override
            protected Map<String, String> getParams() {
                // depending on activity, package different data to be POSTed
                Log.d(TAG, "getParams");
                Map<String, String> params = new HashMap<>();
                if( activity == REGISTER ) {
                    // registering a user
                    params.put("message_type", "register");
                    params.put("user_name", Variables.username);
                    params.put("user_password", Variables.password);
                    params.put("gcm_user_id", Variables.gcm_user_id);
                } else if ( activity == PLATE ) {
                    // registering a plate to a user
                    params.put( "message_type", "plate" );
                    params.put( "user_id", Integer.toString(Variables.user_id) );
                    params.put( "plate_number", Variables.user_plate );
                    params.put( "plate_state", Variables.user_state );
                } else if( activity == LOGIN ) {
                    // logging into a username
                    params.put("message_type", "login");
                    params.put("user_name", Variables.username);
                    params.put("user_password", Variables.password);
                    params.put("gcm_user_id", Variables.gcm_user_id);
                } else if( activity == MESSAGE ) {
                    // sending a message to plate
                    params.put("message_type", "message" );
                    params.put("plate_number", Variables.plate_to );
                    params.put("plate_state", Variables.state_to );
                    params.put("user_sender_id", Integer.toString(Variables.user_id) );
                    params.put("message_sent_timestamp", Variables.time);
                    params.put("gps_lat", Double.toString(Variables.gps_lat) );
                    params.put("gps_lon", Double.toString(Variables.gps_long) );
                    params.put("message_sender_content", Variables.message );
                } else if( activity == READ ) {
                    // acknowledge message received
                    params.put("message_type", "read");
                } else if(activity == REPLY ) {
                    // sending a reply to a message
                    params.put("message_type", "reply");
                }

                Log.d(TAG, "params.toString() : " + params.toString() );
                return params;
            }
        };
        queue.add(postRequest);
    }

    /**
     * @param sResponse String from server converted to JSON and interpreted
     * @param context Context of the activity data was POSTed from
     * @param view used to turn register button back on after error occurs
     * @param activity integer value of the activity data was POSTed from
     * Toast user explaining success/failure, redirects if GUI if success
     */
    private static void interpretResponse(String sResponse, Context context, View view, int activity) {
        Log.d(TAG, "interpretResponse() from server");
        // pop up message Strings and boolean set to fail values by default
        String confirm = "Re-Try", message = "";
        boolean pass = false;

        try{
            // convert server response to JSON
            JSONObject jResponse = new JSONObject(sResponse);
            // check for output key, meaning successful response from server
            if( jResponse.has("output") ) {
                Log.d(TAG, "interpretResponse() = output");

                // get output message, assign confirm message, and set popup boolean to true
                String output = jResponse.get("output").toString();
                pass = true;
                confirm = "Continue";

                // set/clear Variables and set pop up message display dependant on activity
                if( activity == REGISTER ) {
                    // store user id given by server
                    Variables.user_id = (Integer) jResponse.get("user_id");
                    // set pop up message
                    message = "You have registered the name: " + Variables.username +
                            ". Press Continue to register a plate. " + output;
                } else if( activity == PLATE ) {
                    // set popup message
                    message = "The plate: " + Variables.user_plate + " has been " +
                            "register to user: "+ Variables.username + " successfully. " +
                            "Press Continue to access your home screen. " + output;
                } else if( activity == LOGIN ) {
                    // TODO add user_id to response json to put in variables class
                    // TODO currently throws error
                    // store user id response from server
                    //Variables.user_id = (Integer) jResponse.get("user_id");
                    // set popup message
                    message = "Log in successful. Press Continue to access your " +
                    "account. " + output;
                } else if( activity == MESSAGE ) {
                    // set popup message
                    message = "The message: " + Variables.message + " has been " +
                            "sent to plate: "+ Variables.plate_to + Variables.state_to +
                            " successfully. Press Continue to return to your " +
                            "your home screen. " + output;
                    // clear all message data
                    Variables.message = "";
                    Variables.plate_to = "";
                    Variables.state_to = "";
                    Variables.gps_long = 0;
                    Variables.gps_lat = 0;
                    Variables.time = "";
                } else if( activity == READ ) {
                    // TODO add functionality to this
                    message = "Server acknowledges you read your latest message. " +
                            "Press Continue to return to your home screen" + output;
                } else if( activity == REPLY ) {
                    // TODO add functionality to this
                    message = "Server acknowledges you replied to your latest message. " +
                            "Press Continue to return to your home screen" + output;
                }
            } else {
                Log.d(TAG, "interpretResponse() = error: " + jResponse.toString());
                // set up an error message to display to user, default error message is unknown
                String error = "unknown";
                // check for JSON error message from server
                if ( jResponse.has("error") ) {
                    // get error from JSON
                    error = jResponse.get("error").toString();
                    Log.d(TAG, "interpretResponse() = JSON error message: " + error);
                }
                // set popup message
                message = "An error has occurred: " + error + ". " +
                        "Press Re-Try to reattempt to " + actString(activity) + ".";
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSON get error: " + je);
            je.printStackTrace();

            // set popup message
            message = "An error has occurred. " +
                    "Press Re-Try to attempt to " + actString(activity) + ".";
        }
        // display popup to user
        userPopUp(message, confirm, context, activity, pass);
        // turn button back on after an error
        view.setClickable(true);
    }

    /**
     * Create pop up for user to inform about server response/data processing
     * @param message message displayed to user
     * @param confirm acceptance button text
     * @param context Context of the activity where data is being POSTed from
     * @param act integer representation of the activity data is being analyzed from
     * @param pass boolean telling whether method should send to next activity or not
     */
    private static void userPopUp(String message, String confirm, final Context context, final int act, final boolean pass) {
        Log.d(TAG, "userPopUp");

        // create a popup to be displayed to user, describes how data was responded to by server
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setCancelable(false).setPositiveButton(confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "userPopUp : onClick");
                        // move to next activity when data was processed accordingly
                        if( pass ) {
                            Log.d(TAG, "userPopUp: pass");
                            // change to plate confirmation activity
                            if( act == REGISTER ) {
                                // send user to plate registration
                                Intent intent = new Intent(context, ConfirmPlateActivity.class);
                                context.startActivity(intent);
                            } else if( act == PLATE || act == LOGIN || act == MESSAGE) {
                                // send user to home activity
                                Intent intent = new Intent(context, HomeActivity.class);
                                context.startActivity(intent);
                            }
                        }
                    }
                });
        // display message
        builder.create().show();
    }

    /**
     * @param act integer value of the activity data exchange started in
     * @return the String name of the activity, pop up display friendly
     */
    private static String actString(int act) {
        String s = "";
        if(act == REGISTER) {
            s = "register username";
        } else if(act == PLATE) {
            s = "register plate";
        } else if(act == LOGIN) {
            s = "log in";
        } else if(act == MESSAGE) {
            s = "send a message";
        } else if(act == READ) {
            s = "read a message";
        } else if(act == REPLY) {
            s = "reply to a message";
        }
        return s;
    }
}
