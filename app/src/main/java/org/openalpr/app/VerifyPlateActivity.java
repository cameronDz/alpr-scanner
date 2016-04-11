package org.openalpr.app;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 *  Created by Travis
 * Activity for selected plate and state for the message. Gets a list, string[], and picture
 * from the previous activity that are used to display the plate number, confidence, and image.
 *
 * Can re-launce camera to take new picture
 * Submit plate and state to message activity
 *
 */

public class VerifyPlateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String TAG = "VerifyPlateActivity";

    private String mCurrentPhotoPath;

    private ImageView mImageView;

    private String state;

    private String plate;

    private Spinner stateSpinner;

    private Spinner plateSpinner;

    private TextView plateText;

    private TextView errorText;

    private String result;

    private ArrayList<String> candiateList;

    private String[] plateArray = new String[10];

    private int index;

    private LatLng mLatLng;

    private String mTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        errorText = (TextView) findViewById(R.id.errorTextView);
        errorText.setVisibility(View.INVISIBLE);

        plateText = (EditText) findViewById(R.id.plateTextView);

        mImageView = (ImageView) findViewById(R.id.imageView);

        /* gets the variables from previous activity */
        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        candiateList = intent.getStringArrayListExtra("candidateList");
        plateArray = intent.getStringArrayExtra("plateList");
        mCurrentPhotoPath = intent.getStringExtra("picture");
        Bundle bundle = getIntent().getParcelableExtra("latlng");
        mTimeStamp = intent.getStringExtra("timestamp");
        mLatLng = bundle.getParcelable("mlatlng");

        if(mLatLng != null){
            Log.d(TAG, "latlng: (" + mLatLng.latitude + ", " + mLatLng.longitude + ")");
        }

        String[] stateNames = getResources().getStringArray(R.array.states);
        String[] stateAbbreviations = getResources().getStringArray(R.array.states_abbreviated);

        HashMap<String, String> mMap = new HashMap<String, String>();

        for (int i = 0; i < stateNames.length; i++) {
            mMap.put(stateNames[i], stateAbbreviations[i]);
        }

        String currentState = latlngToStateString(mLatLng);
        String currentStateAbbreviated = mMap.get(currentState);

        displayImage();

        // spinner for state code
        stateSpinner = (Spinner) findViewById(R.id.state_spinner);
        stateSpinner.setSelected(false);
        stateSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states_abbreviated,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        if (!currentStateAbbreviated.equals(null)) {
            int spinnerPosition = adapter.getPosition(currentStateAbbreviated);
            stateSpinner.setSelection(spinnerPosition);
        }

        //spinner for plate
        plateSpinner = (Spinner) findViewById(R.id.plate_spinner);
        plateSpinner.setOnItemSelectedListener(this);
        if (intent.getBooleanExtra("recognized", false)) {
            ArrayAdapter<String> plateAdaptor = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item,
                    candiateList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            plateSpinner.setAdapter(plateAdaptor);
        } else {
            setErrorText(getString(R.string.recognition_error));
            errorText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set the error message if the plate recognition was unsucessful
     * @param text
     */
    private void setErrorText(String text) {
        errorText.setText(text);
    }

    /**
     * Method to display the picture taken on the screen
     *
     * Probably a better way is to test the device for its screen size of the imageView
     * and set the image to its dimensions
     */
    private void displayImage() {
        mImageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this)
                .load(new File(mCurrentPhotoPath))
                .resize(600, 600)
                .into(mImageView);
    }

    /**
     * OnItemSelected listener for the two spinners
     * Used to set the plate and state
     * @param parent
     * @param view
     * @param position
     * @param id
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Spinner spinner = (Spinner) parent;

        switch (spinner.getId()) {
            case R.id.state_spinner:
                state = spinner.getSelectedItem().toString();
                Log.d(TAG, "State selected: " + state);
                break;
            case R.id.plate_spinner:
                index = spinner.getSelectedItemPosition();
                Log.d(TAG, "Plate selected: " + plateArray[index]);

                plateText.setText(plateArray[index]);
                plateText.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing when nothing is selected
    }

    /** Called when the user clicks the button to verify the plate */
    public void submitPlate(View view) {
        TextView textView = (TextView) findViewById(R.id.plateTextView);

        Intent intent = new Intent(this, MessageSendActivity.class);
        intent.putExtra("plate", textView.getText().toString().toUpperCase());
        intent.putExtra("state", state);
        intent.putExtra("timestamp", mTimeStamp);
        Bundle args = new Bundle();
        args.putParcelable("mlatlng", mLatLng);

        intent.putExtra("latlng", args);

        startActivity(intent);
    }

    /**
     * Onclick method for entering plate manually if not recognized in image
     * @param view
     */

    public void enterText(View view) {
        TextView textView = (TextView) findViewById(R.id.plateTextView);
        textView.setHint("Enter Plate #");
        textView.setVisibility(View.VISIBLE);

    }

    /**
     * Onclick method for "take picture" button to go back to camera activity to take new picture
     * @param view
     */

    public void takePicture(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public String latlngToStateString(LatLng latlng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String state = "Alabama";
        try{
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            state = addresses.get(0).getAdminArea();
        } catch (IOException e){
            e.printStackTrace();
        }
        return state;
    }
}
