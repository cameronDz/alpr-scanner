package org.openalpr.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Anthony Brignano on 2/14/16.
 *
 * MessageSendActivity: For composing and sending a message
 *      (associated view: activity_message_send)
 *
 *      - sendMessage(View): parses message after user clicks image button
 *      - onCreate(Bundle)
 *      - onStart()
 *      - onEnd()
 *      - onConnected(Bundle): sets mLatLng to LatLng coordinates of users current location
 *      - onConnectionSuspended(int)
 *      - onConnectionFailed(ConnectionResult)
 *
 * TODO: Add method to save which image was selected to a text file on users device
 *
 * date@(18.03.2016) @editor(cameronDz)
 * Added check for empty JSON being sent to server. Tested to make sure JSON
 * Object was being interpreted and assembled correctly, made slight changes
 * using toString() method for checking for empty Object.
 *
 * date@(19.03.2016) editor@(cameronDz)
 * Added server timeout error listener to the POST response. Rearranged how
 * message data is gathered for sending a message and creating JSON.
 *
 * date@(20.03.2016) editor@(cameronDz)
 * Added AlertDialog popups to all errors and server processing where the
 * process might be interrupted, an error may occur, and when user is
 * successfully sends a message.
 */

public class MessageSendActivity extends AppCompatActivity {

    private String TAG = "MessageSendActivity";
    private Context context;
    // message data
    private String state;
    private String plate;
    private String message;
    private double gpsLong;
    private double gpsLat;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_send);
        context = this;

        // Get the plate and save to activity
        Intent intent = getIntent();
        state = intent.getStringExtra("state");
        plate = intent.getStringExtra("plate");
        // Get gps coordinates of incident
        gpsLat = 0;
        gpsLong = 0;
        // TODO get real gps coordinates
        // get timestamp of incident
        time = "2/20/2016 9:00:50";
        // TODO get real timestamp

        Log.d(TAG, "STATE: " + state);
        Log.d(TAG, "PLATE: " + plate);
    }

    public void sendMessage(View view) {
        Log.d(TAG, "sendMessage Button Pressed");
        ImageButton message_button = (ImageButton) view;
        message = message_button.getContentDescription().toString();

        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,
                message + "\nPlate: " + plate + "\nState: " + state,
                duration);
        toast.show();

        // send message to server
        sendMessageToServer();
    }

    private void sendMessageToServer() {
        Log.d(TAG, "sendMessageToServer");

        // requests queue to be sent to server
        RequestQueue queue = Volley.newRequestQueue(this);
        // JSONObject to be sent
        JSONObject json = formatJSONMessage(plate, state, message, time, gpsLong, gpsLat);

        // checks to make sure JSON object has data in it
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

                                            // display pop up to user informing of server timeout
                                            String message = "There may be a problem with the " +
                                                    "server. Please try logging in again. Press " +
                                                    "Re-Try to reattempt to send your message.";
                                            String confirm = "Re-Try.";
                                            userPopUp(message, confirm);
                                        }
                                    } else {
                                        Log.d(TAG, "Error: server problem");

                                        // display pop up to user informing of server issue
                                        // usual error is no internet access
                                        String message = "There may be a problem with your " +
                                                "internet access. Please check your connection " +
                                                "to the internet and press Re-Try to " +
                                                "reattempt to send your message.";
                                        String confirm = "Re-Try.";
                                        userPopUp(message, confirm);
                                    }
                                }
                            });

            // new request added to queue
            queue.add(jsonRequest);
        } else {
            Log.d(TAG, "json variable empty error");

            // display pop up informing user of data problem
            String message = "There may be a problem with processing you data. " +
                    "Please press Re-Try to reattempt to send your message.";
            String confirm = "Re-Try.";
            userPopUp(message, confirm);
        }
    }

    /**
     * Takes response from server and tells user if message was sent successfully
     * @param response servers response to the JSON
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse");

        // attempt to breakdown JSON object
        try {
            // get message status from JSON object
            String messResp = response.get("status").toString();
            // TODO TEST make sure server is returning expected JSON "status"
            if( messResp.equals("success") ) {
                Log.d(TAG, "interpretResponse: success");

                // display pop up informing user of successful plate registration
                // TODO add message sent and plate sent to message details
                String message = "The message: " + "INSERT_MESSAGE" + " has been " +
                        "sent to plate: "+ "INSERT_PLATE" + " successfully. " +
                        "Press Continue to access your home screen.";
                String confirm = "Continue.";
                userPopUp(message, confirm);

                // TODO erase plate sent to message details from device

                // send user to home activity
                Intent intent = new Intent(this, ConfirmPlateActivity.class);
                startActivity(intent);
            } else {
                // assume "status : failed"
                Log.d(TAG, "interpretResponse: failed");

                // get a possible error from JSON
                String error = response.get("error").toString();
                // display pop up to user bad plate registration data
                String message = "There was a problem with your message. " +
                        "The error was: " + error + ". " +
                        "Press Re-Try to reattempt to send your message.";
                String confirm = "Re-Try.";
                userPopUp(message, confirm);
            }
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);

            // display pop up to user informing of error
            String message = "There was an error processing the server response. " +
                    "Sorry for the inconvenience. Press Re-Try to reattempt to send message.";
            String confirm = "Re-Try.";
            userPopUp(message, confirm);
        }
    }

    /**
     * Prepares JSON object message to be sent out another user
     * @param plate plate number to send a message to
     * @param state state of plate to send a message to
     * @param message message identifier
     * @param gpsLong longitudinal coordinates where plate picture was taken
     * @param gpsLat latitudinal coordinates where plate picture was taken
     * @return a JSON object to be sent out to server, on error, sends an
     *         empty JSON object
     */
    private JSONObject formatJSONMessage(String plate, String state, String message,
                                         String time, double gpsLong, double gpsLat) {
        Log.d(TAG, "formatJSONMessage");
        JSONObject json = new JSONObject();

        // attempt to put message into JSON object
        try {
            json.put("messageType","message");
            json.put("plate_number", plate);
            json.put("plate_state", state);
            json.put("user_sender_id", Variables.user_id);
            json.put("message_sent_timestamp", time);
            json.put("gps_lat", gpsLat);
            json.put("gps_lon", gpsLong);
            json.put("message_sender_content", message);
            // TODO TEST that this is how server is expecting JSON
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);

            // json is returned as an empty object if error occurs
            json = new JSONObject();
        }
        Log.d(TAG, "formatJSONMessage result: " + json.toString() );
        return json;
    }

    /**
     * Create pop up for user to inform about server response or data processing
     * @param message message displayed to user
     * @param confirm acceptance button text
     */
    private void userPopUp(String message, String confirm) {
        Log.d(TAG, "errorPopUp");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setCancelable(false).setPositiveButton(confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "errorPopUp : onClick");
                        // do nothing
                    }
                });
        // display message
        builder.create().show();
    }
}
