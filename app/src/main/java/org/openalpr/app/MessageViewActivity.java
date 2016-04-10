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
 */
public class MessageViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "MessageViewActivity";
    private Context context;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);
        context = this;

        TextView datetime_text =(TextView)findViewById(R.id.text_datetime);
        TextView location_text =(TextView)findViewById(R.id.text_location);
        ImageView img_message =(ImageView)findViewById(R.id.img_message);
        String datetime = new SimpleDateFormat("EEE, MMM dd, ''yy 'at' hh:mm:ss a", Locale.ENGLISH).format(new Date());
        datetime_text.setText(datetime);
        location_text.setText("(-34.4531, 151.5421)");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void sendFeedback(View view) {
        ImageButton feedback_button = (ImageButton) view;
        String message = feedback_button.getContentDescription().toString();
        // displays message to user
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));

    }
}