package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Anthony Brignano on 2/23/16.
 *
 * ConfirmPlateActivity: For registration of new plate
 *      (associated view activity_confirm_plate)
 *
 *      - confirmPlate(View): verifies plate credentials and redirects user to HomeActivity.java (activity_home.xml)
 *      - onItemSelected(AdapterView): sets plate_state variable to the state selected (from the spinner)
 *      - onNothingSelected(AdapterView)
 *      - onCreate(Bundle)
 *
 * version@(7.3.2016) user@(cameronDz)
 */

public class ConfirmPlateActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private String TAG = "ConfirmPlateActivity";
    private Context context;
    private String plate_state = "";
    protected String plate_number = "";
    protected GoogleCloudMessaging gcm = null;

    private String[] abrv_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        context = this;

        Log.v(TAG, "TEST TEST TEST: ");
        Log.v(TAG, "TEST TEST TEST: Variables.username = " + Variables.username);
        Log.v(TAG, "TEST TEST TEST: Variables.password = " + Variables.password);
        Log.v(TAG, "TEST TEST TEST: ");

        // get the abrv. version of the states for sending to db
        Resources res = getResources();
        abrv_state = res.getStringArray(R.array.states_abbreviated);
    }

    public void confirmPlate(View view) {
        EditText p = (EditText)findViewById(R.id.plate_number);
        plate_number = p.getText().toString();
        context = getApplicationContext();

        gcm = GoogleCloudMessaging.getInstance(context);

        Log.d(TAG, "Plate Number: " + plate_number);
        Log.d(TAG, "Plate State: " + plate_state);

        // for sending gcm
        Variables.user_plate = this.plate_number;
        Variables.user_state = this.plate_state;

        // Logic needs to be added to this variable based on the database interaction(s)
        Boolean plateConfirmationComplete = true;

        if(plateConfirmationComplete){

            //gcm sends hi message to server
            Log.v("GCM_PRINT gcm: ", gcm.toString());
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "Sent message";
                    try {
                        Bundle data = new Bundle();
                        data.putString("messageType", "register_user");
                        data.putString("username", Variables.username);
                        data.putString("password", Variables.password);
                        data.putString("plateString", Variables.user_plate);
                        data.putString("plateState", Variables.user_state);
                        String id = Integer.toString(Constants.MSG_ID) + "alpr";
                        Constants.MSG_ID++;
                        Log.v(TAG, "GCM_SEND BEFORE_TOKEN: " + Constants.REG_TOKEN);
                        Log.v(TAG, "GCM_SEND BEFORE_PROJECT_ID: " + Constants.PROJECT_ID);
                        gcm.send(Constants.PROJECT_ID + "@gcm.googleapis.com", id, data);
                        Log.v(TAG, "GCM_SEND AFTER_data: " + data.toString());
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                        Log.v(TAG, "GCM_SEND " + msg);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        msg = "Error : " + e.getMessage();
                        Log.v(TAG, "GCM_SEND " + msg);
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    // mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);


            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        //Object s = parent.getItemAtPosition(pos);
//        plate_state = s.toString();
        int s = parent.getSelectedItemPosition();
        plate_state = abrv_state[s];

        Log.d(TAG, "abrv_state: " + plate_state);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        String message = "Error: No state selected.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }



}