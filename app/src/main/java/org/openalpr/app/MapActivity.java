package org.openalpr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Anthony Brignano on 3/27/16.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Context context;
    private static String TAG = "MapActivity";
    private GoogleMap mMap;
    private final int MID = 0;
    private final int TIMESTAMP = 1;
    private final int GPS_LON = 2;
    private final int GPS_LAT = 3;
    private final int MESSAGE = 4;
    private ArrayList<MessageItem> messages;
    private ArrayList<String> messageStringList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the arrayList of strings saved to file
        messageStringList = Variables.messages(context);

        Log.d(TAG, "message list size = " + messageStringList.size());

        // create arrayList of message objects from the strings
        messages = new ArrayList<MessageItem>();
        loadMessages(messageStringList);
    }

    protected void onStart() {
        super.onStart();
        Log.d(TAG, "In onStart");

        /*// Get the arrayList of strings saved to file
        messageStringList = Variables.messages(context);

        Log.d(TAG, "message list size = " + messageStringList.size());

        // create arrayList of message objects from the strings
        messages = new ArrayList<MessageItem>();
        loadMessages(messageStringList);*/


        // load them on the screen
//        displayMarkers(mMap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "In onMapReady");
        mMap = googleMap;
        
        displayMarkers(mMap);

    }

    public void displayMarkers(GoogleMap googleMap){
        mMap = googleMap;

        for(MessageItem message : messages){
            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(message.getGpsLat()), Double.parseDouble(message.getGpsLon())))
                            .title(message.getMid()));
        }
    }

    // fill the arraylist for view
    private void loadMessages(ArrayList<String> messageStringList) {
        //      Log.d(TAG, "In loadMessages");

        for(int i = 0; i < messageStringList.size(); i++) {
            String[] mess = messBreakDown(messageStringList.get(i));
            MessageItem messageItem = new MessageItem();

            messageItem.setMid(mess[MID]);
            messageItem.setTimestamp(mess[TIMESTAMP]);
            messageItem.setGpsLon(mess[GPS_LON]);
            messageItem.setGpsLat(mess[GPS_LAT]);
            messageItem.setMessage(mess[MESSAGE]);
            messageItem.setReadFalse();

            messages.add(messageItem);
        }

        messageStringList.clear();
    }

    /**
     * Breaks down a stored string representation of a JSON Object of a message
     * into the components of the message
     * @param message the stored string
     * @return s[0] = mid = the unique message id
     *         s[1] = timestamp = time message was sent
     *         s[2] = gps_lon = longitude gps coordinate
     *         s[3] = gps_lat = latitude gps coordinate
     *         s[4] = message = actually message that was sent
     */
    protected static String[] messBreakDown(String message) {
        //     Log.d(TAG, "In messBreakDown");
        Log.d(TAG, "messageBreakDown: " + message);
        String[] s = new String[5];

        // attempt to break string into components
        try {
            JSONObject json = new JSONObject(message);

            s[0] = json.get("mid").toString();
            s[1] = json.get("timestamp").toString();
            s[2] = json.get("gps_lon").toString();
            s[3] = json.get("gps_lat").toString();
            s[4] = json.get("message").toString();

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
            e.printStackTrace();
        }

        return s;
    }

}
