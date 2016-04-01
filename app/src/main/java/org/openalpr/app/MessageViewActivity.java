package org.openalpr.app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    private LatLng test_address = new LatLng(41.721682, -72.781755);

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

        String addr_text = latlngToAddressString(test_address);

        location_text.setText(addr_text);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public String latlngToAddressString(LatLng latlng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String address = "(" + latlng.latitude + ", " + latlng.longitude + ")";
        try{
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String street = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            address = street + " " + city + ", " + state;
        } catch (IOException e){
            e.printStackTrace();
        }
        return address;
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

        // Add a marker to the test_address (my house)
        mMap.addMarker(new MarkerOptions().position(test_address).title("Test Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test_address));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(test_address, 12.0f));

    }
}