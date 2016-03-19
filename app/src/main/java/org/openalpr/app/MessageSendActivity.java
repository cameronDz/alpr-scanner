package org.openalpr.app;

import android.content.Context;
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
 * Added server timeout error listener to the POST response
 */

public class MessageSendActivity extends AppCompatActivity {

    private String TAG = "MessageSendActivity";
    private Context context;
    private String state;
    private String plate;
    private String message;
    // TODO get gps coordinates

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
        JSONObject json = formatJSONMessage(plate, state, message, "2/20/2016 9:00:50", 0, 0);
        // TODO come up with a non-static timestamp
        // TODO come up with real gps coordinates

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
     * Takes response from server and tells user if message was sent successfully
     * @param response servers response to the JSON
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse");

        // attempt to breakdown JSON object
        try {
            // get message status from JSON object
            String message = response.get("status").toString();
            // TODO TEST make sure server is returning expected JSON "status"
            if( message.equals("success") ) {
                Log.d(TAG, "interpretResponse: success");

                // TODO add a toast or popup informing user of success

                // send user to home activity
                Intent intent = new Intent(this, ConfirmPlateActivity.class);
                startActivity(intent);
            } else {
                // assume "status : failed"
                Log.d(TAG, "interpretResponse: failed");

                // TODO put Toast here informing user of failure
                // TODO get server JSON error response protocol
            }
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);

            // TODO add error Toast to user
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
}
