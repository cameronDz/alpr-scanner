package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import android.os.AsyncTask;
=======
>>>>>>> gcm
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
>>>>>>> master
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


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
 * TO DO:
 *  - Add method to save which image was selected to a text file on users device
 */

public class MessageSendActivity extends AppCompatActivity {
    private Context context;

    private String TAG = "MessageSendActivity";

    private String state;

    private String plate;

<<<<<<< HEAD

=======
<<<<<<< HEAD
    private String message;

    private GoogleCloudMessaging gcm = null;
=======

>>>>>>> master
>>>>>>> gcm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        ImageButton message_button = (ImageButton) view;
        message = message_button.getContentDescription().toString();


        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,
                message + "\nPlate: " + plate + "\nState: " + state,
                duration);
        toast.show();

        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "Sent message";
                try {
                    Bundle data = new Bundle();
                    // sending a message
                    data.putString("messageType", "message");
                    data.putString("username", Variables.username);
                    data.putString("password", Variables.password);
                    data.putString("plateString", plate);
                    data.putString("plateState", state);
                    data.putString("uidFrom", "1");
                    data.putString("timestamp", "2/20/2016 9:00:50");
                    data.putString("message", message);
                    data.putString("gpsLat", "-45.0001");
                    data.putString("gpsLon", "20.0204");
                            /* sending a new registering user
                            data.putString("messageType", "register_user");
                            data.putString("username", "test_user000");
                            data.putString("password", "test_pass");
                            data.putString("plateString", "ABC123");
                            data.putString("plateState", "CT");
                            */
                    String id = Integer.toString(Constants.MSG_ID) + "unique3";
                    Constants.MSG_ID++;
                    Log.v("GCM_SEND", "BEFORE");
                    Log.v("GCM_SEND", "BEFORE_TOKEN: " + Constants.REG_TOKEN);
                    Log.v("GCM_SEND", "BEFORE_PROJECT_ID: " + Constants.PROJECT_ID);
                    gcm.send(Constants.PROJECT_ID + "@gcm.googleapis.com", id, data);
                    Log.v("GCM_SEND", "AFTER gcm: " + gcm.toString());
                    Log.v("GCM_SEND", "AFTER data: " + data.toString());
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
}



}
