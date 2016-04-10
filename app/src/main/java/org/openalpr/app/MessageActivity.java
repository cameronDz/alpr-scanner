package org.openalpr.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;


/**
 *
 * CAN BE DELETED - REPLACED BY MessageSendActivity.java (needed a more specific class name)
 *
 * Created by Anthony Brignano on 2/14/16.
 *
 * MessageActivity: For composing and sending a message
 *      (associated view: activity_message)
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
 */

public class MessageActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "MessageActivity";
    private LatLng mLatLng = null;
    private Location mLastLocation = null;
    private String state;
    private String plate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_send);
        context = this;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get the plate and save to activity
        Intent intent = getIntent();
        state = intent.getStringExtra("state");
        plate = intent.getStringExtra("plate");

        Log.d(TAG, "STATE: " + state);
        Log.d(TAG, "PLATE: " + plate);
    }

    public void sendMessage(View view){
//        ImageButton message_button = (ImageButton)view;
//        String message = message_button.getContentDescription().toString();
//        Double lng = mLatLng.longitude;
//        Double lat = mLatLng.latitude;
//        String latlng = "(" + lng.toString() + ", " + lat.toString() + ")";
//        // untested line below
//        // message += latlng;
//        int duration = Toast.LENGTH_SHORT;
//
//        // displays message to user
//        Toast toast = Toast.makeText(context, message, duration);
//        toast.show();
    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        // check if API version is 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // check if permission is granted
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }
        else{
            // if API version is not 23 just set mLastLocation
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        // set LatLng variable -- not sure if this is necessary, mLastLocation already exists as a Location variable
        if (mLastLocation != null) {
            mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    }
    @Override
    public void onConnectionSuspended(int i){
        // code to be run when connection is stopped
    }
    @Override
    public void onConnectionFailed(ConnectionResult cr){
        // code to be run when connection fails
    }
    protected void createLocationRequest() {
        /**
         * Performance hint: If your app accesses the network or does other long-running work
         * after receiving a location update, adjust the fastest interval to a slower value.
         * This adjustment prevents your app from receiving updates it can't use.
         * Once the long-running work is done, set the fastest interval back to a fast value.
         */
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }
}
