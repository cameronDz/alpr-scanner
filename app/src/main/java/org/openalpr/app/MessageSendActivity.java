package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


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

    public void sendMessage(View view){
        ImageButton message_button = (ImageButton)view;
        String message = message_button.getContentDescription().toString();


        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,
                message + "\nPlate: " + plate + "\nState: " + state,
                duration);
        toast.show();
    }


}
