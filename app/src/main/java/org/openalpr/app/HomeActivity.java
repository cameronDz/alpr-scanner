package org.openalpr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

// some of these might not be needed
import android.location.LocationManager;

/**
 * Created by Anthony Brignano on 2/26/16.
 *
 * HomeActivity: Allows for basic navigation between application views
 *      (associated view activity_home)
 *
 *      - redirectToInbox(View): redirects user to InboxActivity.java (activity_inbox.xml) on click
 *      - redirectToCamera(View): redirects user to CameraActivity.java (activity_camera.xml) on click
 *      - onCreate(Bundle)
 *
 * date@(7.3.2016) editor@(cameronDz)
 * Set welcome message to include globally stored username
 */

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate method");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Locate and set TextView that displays welcome message
        TextView welcome_text =(TextView)findViewById(R.id.text_welcome);
        String message = "Welcome " + Variables.username + "!";
        welcome_text.setText(message);
    }

    public void redirectToInbox(View view) {
        Log.d(TAG, "Inbox Button Pressed");
        Intent intent = new Intent(this, InboxActivity.class);
        startActivity(intent);
    }

    public void redirectToCamera(View view) {
        Log.d(TAG, "Camera Button Pressed");
        
        // detect whether location is on or not
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);         
        if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
            // send user to camera view
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else {
            // notify user to turn location on
            Toast.makeText(this, "Turn GPS location on to use Camera Mode", Toast.LENGTH_SHORT).show();
        }
    }

    public void redirectToMap(View view) {
        Log.d(TAG, "Map Button Pressed");
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void redirectToConfirmPlate(View view) {
        Log.d(TAG, "Confirm Plate Button Pressed");
        Intent intent = new Intent(this, ConfirmPlateActivity.class);
        startActivity(intent);
    }
}
