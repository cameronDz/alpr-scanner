package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;

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
 * TODO: Add method to save which image was selected to a text file on users device
 *
 * date@(23.03.2016) editor@(cameronDz)
 * Removed all methods and calls within class dealing with sending data to
 * the server, and replaced with one call in the sendMessage listener that
 * sends all HTTP POST and server response interpretation to the HTTPService
 * class. Class handles all errors and redirects depending. Also removed all
 * old date/editor comments pertaining to server interaction, since no longer
 * relevant to this class.
 */

public class MessageSendActivity extends AppCompatActivity {

    private String TAG = "MessageSendActivity";
    private LatLng mLatLng;

    private Context context;
    // message data

    protected String state;
    
    protected String plate;
    
    protected String message;

    protected double gpsLon;
    
    protected double gpsLat;
    
    protected String time;

    private String mTimeStamp;

    private GoogleCloudMessaging gcm = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_send);
        // set context, used in sending data to server
        context = this;

        // Get the plate and save to activity
        Intent intent = getIntent();
        state = intent.getStringExtra("state");
        plate = intent.getStringExtra("plate");
        mTimeStamp = intent.getStringExtra("timestamp");

        Bundle bundle = intent.getParcelableExtra("latlng");
        mLatLng = bundle.getParcelable("mlatlng");
        // Get gps coordinates of incident
        if(mLatLng != null){
            gpsLat = mLatLng.latitude;
            gpsLon = mLatLng.longitude;
        }
        Log.d(TAG, "GPS IN MESSAGE SEND: (" + gpsLat + ", " + gpsLon + ")");
        // get timestamp of incident
        time = mTimeStamp;

        Log.d(TAG, "STATE: " + state);
        Log.d(TAG, "PLATE: " + plate);
        Log.d(TAG, "TIMESTAMP: " + time);
    }

    public void sendMessage(View view) {
        Log.d(TAG, "sendMessage Button Pressed");
        ImageButton message_button = (ImageButton) view;
        // TODO change this to an int
        message = message_button.getContentDescription().toString();

        // store all variables globally
        Variables.plate_to = plate;
        Variables.state_to = state;
        Variables.message = message;
        Variables.time = time;
        Variables.gps_lat = gpsLat;
        Variables.gps_long = gpsLon;

        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,
                message + "\nPlate: " + plate + "\nState: " + state,
                duration);
        toast.show();

        // make button unclickable to avoid sending multiple registrations
        view.setClickable(false);
        // data sent out to server using Volley HTTP POST. attempts to store
        // message in database. sends user to activity according to response
        HTTPService.sendData(context, view, 4);
    }
}
