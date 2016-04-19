package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anthony Brignano on 2/23/16.
 *
 * ConfirmPlateActivity: For registration of new plate
 *      (associated view activity_confirm_plate)
 *
 *      - confirmPlate(View): verifies plate credentials and redirects
 *          user to HomeActivity.java (activity_home.xml)
 *      - onItemSelected(AdapterView): sets plate_state variable to
 *          the state selected (from the spinner)
 *      - onNothingSelected(AdapterView)
 *      - onCreate(Bundle)
 *
 * date@(23.03.2016) editor@(cameronDz)
 * Removed all methods and calls within class dealing with sending data to
 * the server, and replaced with one call in the ConfirmPlate listener that
 * sends all HTTP POST and server response interpretation to the HTTPService
 * class. Class handles all errors and redirects depending. Also removed all
 * old date/editor comments pertaining to server interaction, since no longer
 * relevant to this class.
 */

public class ConfirmPlateActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private String TAG = "ConfirmPlateActivity";
    private Context context;
    private String plate_state = "";
    protected String plate_number = "";
    boolean specialCharCheck = true;

    private String[] abrv_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_plate);

        Spinner spinner = (Spinner) findViewById(R.id.plate_state_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        // set context, used in sending data to server
        context = this;

        Log.v(TAG, "TEST TEST TEST: ");
        Log.v(TAG, "TEST TEST TEST: Variables.username = " + Variables.username);
        Log.v(TAG, "TEST TEST TEST: Variables.password = " + Variables.password);
        Log.v(TAG, "TEST TEST TEST: ");

        // get the abrv. version of the states for sending to db
        Resources res = getResources();
        abrv_state = res.getStringArray(R.array.states_abbreviated);

    }

    /**
     * User presses confirm plate, data sent out to server to check plate
     * @param view button pressed to confirm plate
     */
    public void confirmPlate(View view) {
        Log.d(TAG, "ConfirmPlate Button Pressed");
        EditText plate = (EditText) findViewById(R.id.plate_number);
        plate_number = plate.getText().toString();

        // checks if String only contains letters and/or numbers (no special characters)
        Pattern p = Pattern.compile("\\w", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(plate_number);
        specialCharCheck = m.find();

        Log.d(TAG, "plate_number: " + plate_number);
        Log.d(TAG, "specialCharCheck: " + specialCharCheck);

        if(plate_number.length() > 0 && plate_number.length() < 9 && specialCharCheck) {
            // store user selected plate/state in global variables
            Variables.user_plate = this.plate_number;
            Variables.user_state = this.plate_state;

            // make button unclickable to avoid sending multiple registrations
            view.setClickable(false);
            // data sent out to server using Volley HTTP POST. determines if plate
            // is available and sends user to activity according to response
            HTTPService.sendData(context, view, 2);
        }
        else if(!specialCharCheck){
            Log.d(TAG, "Plate contains special characters");
            int duration = Toast.LENGTH_SHORT;
            // displays message to user if plate has a special character in it
            String message = "License plates do not contain special characters.";
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }
        else if(plate_number.length() <= 0){
            Log.d(TAG, "Plate length <= 0");
            int duration = Toast.LENGTH_SHORT;
            // displays message to user if plate is too short
            String message = "You must enter a license plate number.";
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }
        else{
            Log.d(TAG, "Plate length >= 9");
            int duration = Toast.LENGTH_SHORT;
            // displays message to user if plate is too long
            String message = "License plates cannot be more than 8 characters.";
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }


    }


    /**
     * Determines the state for the license plate by getting it from a spinner which
     * a user selects from on the GUI
     * @param parent AdapterView for spinner
     * @param view   View spinner is in
     * @param pos    position of spinner -- state being selected
     * @param id     id in XML of spinner
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using

        // get the abrv version of the plate from the displayed full state name drop down
        int s = parent.getSelectedItemPosition();
        plate_state = abrv_state[s];

        Log.d(TAG, "abrv_state: " + plate_state);

    }

    /**
     * Error message for no state being selected.
     * @param parent Adapter View for spinner
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // interface callback
        String message = "Error: No state selected.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
