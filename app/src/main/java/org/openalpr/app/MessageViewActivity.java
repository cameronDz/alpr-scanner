package org.openalpr.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Anthony Brignano on 3/25/16.
 *
 * MessageViewActivity: For viewing, and submitting feedback for, a message
 *      (associated view: activity_message_view)
 *
 * - sendFeedback(View): submits user feedback to a received message (negative, neutral, positive)
 * - onCreate(Bundle)
 * - onMapReady(GoogleMap)
 *
 * Edit by Travis Moretz 4/9/16
 *
 * Takes income message from inbox and displays values, if the message has been replied to then
 * does not let user reply again.
 *
 * Puts pin on map and looms to location of message
 *
 *
 */
public class MessageViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "MessageViewActivity";
    private Context context;
    private GoogleMap mMap;

    MessageItem  messageItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);
        context = this;


        // get the incoming message from inbox
        messageItem = getIntent().getExtras().getParcelable("messageItem");

        TextView datetime_text =(TextView)findViewById(R.id.text_datetime);
        TextView location_text =(TextView)findViewById(R.id.text_location);
        ImageView img_message =(ImageView)findViewById(R.id.img_message);
//        String datetime = new SimpleDateFormat("EEE, MMM dd, ''yy 'at' hh:mm:ss a", Locale.ENGLISH).format(new Date());

        // Set the time values from message
        String datetime = messageItem.getTimestamp();
        String gpsLon = messageItem.getGpsLon();
        String gpsLat = messageItem.getGpsLat();
        datetime_text.setText(datetime);
        location_text.setText(gpsLon + ", " + gpsLat);

        // to set the correct message image
        // fill the view with correct image
        ImageView imageView = (ImageView) findViewById(R.id.img_message);

        switch (messageItem.getMessage()) {
            case "1":
                imageView.setImageResource(R.drawable.mad);
                break;
            case "2":
                imageView.setImageResource(R.drawable.thankyou);
                break;
            case "3":
                imageView.setImageResource(R.drawable.slowdown);
                break;
            case "4":
                imageView.setImageResource(R.drawable.no_parking);
                break;
            case "5":
                imageView.setImageResource(R.drawable.cellphone);
                break;
            case "6":
                imageView.setImageResource(R.drawable.mechanic);
                break;
        }


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void sendFeedback(View view) {
        ImageButton feedback_button = (ImageButton) view;
        String message = feedback_button.getContentDescription().toString();

        String noRelyMessage = "Message feedback already sent!";
        // displays message to user if no feedback given yet
        if (messageItem.getReplyStatus() == false) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
            // set the message to replied
            messageItem.setReplyTrue();
        // else feedback already sent, don't send again
        } else {
            Toast.makeText(context, noRelyMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add current marker on map to message gps location
        double lat = Double.parseDouble(messageItem.getGpsLat());
        double lon = Double.parseDouble(messageItem.getGpsLon());
        LatLng currentMessageLocal = new LatLng(lat, lon);

        // display on map
        mMap.addMarker(new MarkerOptions().position(currentMessageLocal).title(messageItem.getTimestamp()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentMessageLocal));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMessageLocal, 12.0f));

    }
}