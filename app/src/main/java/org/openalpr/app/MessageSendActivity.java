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
 * date@(17.03.2016) @editor(cameronDz)
 *
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
        // Server address and JSONObject to be sent
        // TODO TEST AWS address, make sure hard coding it is 'safe'
        String address = "http://107.21.62.238/";
        // TODO make address a constant global variable
        JSONObject json = formatJSONMessage(plate, state, message, 0, 0);
        // TODO get real gps coordinates
        // TODO add a check to make sure json variable is not NULL

        // new request to be sent out to server
        Log.d(TAG, "create and send JSON POST request");
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.POST, address, json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // break down JSON response from server, send user to new
                                // activity if successful registration, or inform of fail
                                interpretResponse(response);
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
     * Takes response from server and tells user if message was sent successfully
     * @param response servers response to the JSON
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse");

        // attempt to breakdown JSON object
        try {
            // TODO make sure server is returning expected JSON (ask Connor or Matt)

            // TODO check to make sure breaking down JSON correctly
            // get message status from JSON object
            String login = response.get("status").toString();
            if( login.equals("success") ) {
                Log.d(TAG, "login: success");

                // TODO check to make sure breaking down JSON correctly
                // set global username
                Variables.username = response.get("username").toString();

                // TODO add a toast or popup informing user of success

                // send user to home activity
                Intent intent = new Intent(this, ConfirmPlateActivity.class);
                startActivity(intent);
            } else if( login.equals("fail") ) {
                Log.d(TAG, "message: failed");

                // TODO put Toast here informing user of failure
            }
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);
        }

    }

    /**
     * Prepares JSON object message to be sent out another user
     * @param plate plate number to send a message to
     * @param state state of plate to send a message to
     * @param message message identifier
     * @param gpsLong longitudinal coordinates where plate picture was taken
     * @param gpsLat latitudinal coordinates where plate picture was taken
     * @return a JSON object to be sent out to server
     */
    private JSONObject formatJSONMessage(String plate, String state, String message,
                                         double gpsLong, double gpsLat) {
        Log.d(TAG, "formatJSONMessage");
        JSONObject json = new JSONObject();

        // attempt to put message into JSON object
        try {
            json.put("messageType","message");
            json.put("plate_number", plate);
            json.put("plate_state", state);
            json.put("user_sender_id", Variables.user_id);
            // TODO come up with a non-static timestamp
            json.put("message_sent_timestamp","2/20/2016 9:00:50");
            // TODO come up with real gps coordinates
            json.put("gps_lat", gpsLat);
            json.put("gps_lon", gpsLong);
            json.put("message_sender_content", message);
            // TODO TEST that this is how server is expecting JSON
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSONException: " + je);
        }

        // TODO find a way to not return null on error
        return json;
    }
}
