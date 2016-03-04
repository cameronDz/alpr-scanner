package org.openalpr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.openalpr.app.AppConstants.JSON_RESULT_ARRAY_NAME;


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

    private AlprResult alprResult;

    private String result;

    private ArrayList<String> candiateList;

    private String[] plateArray = new String[10];

    private int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        errorText = (TextView) findViewById(R.id.errorTextView);
        errorText.setVisibility(View.INVISIBLE);

        plateText = (EditText) findViewById(R.id.plateTextView);




        mImageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        candiateList = intent.getStringArrayListExtra("candidateList");
        plateArray = intent.getStringArrayExtra("plateList");
        mCurrentPhotoPath = intent.getStringExtra("picture");
        displayImage();


        /**
         * spinner for state code
         */
        stateSpinner = (Spinner) findViewById(R.id.state_spinner);
        stateSpinner.setSelected(false);
        stateSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states_abbreviated,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);


        /**
         * spinner for plate
         */

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

    private void setErrorText(String text) {
        errorText.setText(text);
    }

    private void displayImage() {
        mImageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this)
                .load(new File(mCurrentPhotoPath))
                .resize(600, 600)
                .into(mImageView);
    }

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
        /**
         * do nothing when nothing is selected
         */
    }

    /** Called when the user clicks the button to verify the plate */
    public void submitPlate(View view) {

        Intent intent = new Intent(this, MessageSendActivity.class);
        intent.putExtra("plate", plateArray[index]);
        intent.putExtra("state", state);
        startActivity(intent);
    }

    public void enterText(View view) {
        TextView textView = (TextView) findViewById(R.id.enter_text);
        textView.setHint("Enter Plate #");
        textView.setVisibility(View.VISIBLE);
    }
}