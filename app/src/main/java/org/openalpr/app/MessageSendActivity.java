package org.openalpr.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_send);
        context = this;
    }

    public void sendMessage(View view){
        ImageButton message_button = (ImageButton)view;
        String message = message_button.getContentDescription().toString();

        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }


}