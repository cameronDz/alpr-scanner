package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
 * date@(07.03.2016) editor@(cameronDz)
 * Added GCM upstream feature. Used to register a user and a plate.
 *
 * date@(17.3.2016) editor@(cameronDz)
 * Removed GCM upstream feature and replaced with HTTP Volley POST
 * request that handles responses from server as well. Put logs in
 * methods. Volley needs to be tested.
 *
 * date@(18.03.2016) editor@(cameronDz)
 * Added check for empty JSON being sent to server.
 *
 * date@(19.03.2016) editor@(cameronDz)
 * Added server timeout error listener to the POST response
 */

public class ConfirmPlateActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private String TAG = "ConfirmPlateActivity";
    private Context context;
    private String plate_state = "";
    protected String plate_number = "";

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

        context = this;
    }

    /**
     * User presses confirm plate, data sent out to server to check plate
     * @param view current view: plate confirmation view
     */
    public void confirmPlate(View view) {
        Log.d(TAG, "ConfirmPlate Button Pressed");
        EditText p = (EditText)findViewById(R.id.plate_number);
        plate_number = p.getText().toString();
        context = getApplicationContext();

        // store user selected plate/state in global variables
        Variables.user_plate = this.plate_number;
        Variables.user_state = this.plate_state;

        // data sent out to server using Volley HTTP POST. determines if plate
        // is available and sends user to activity according to response
        sendDataToServer();
    }

    /**
     * Determines the state for the license plate by getting it from a spinner which
     * a user selects from on the GUI
     * @param parent AdapterView for spinner
     * @param view View spinner is in
     * @param pos position of spinner -- state being selected
     * @param id id in XML of spinner
     */
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //  retrieve the selected item from spinner
        Object s = parent.getItemAtPosition(pos);
        plate_state = s.toString();
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

    /**
     * Used to send plate registration data to server
     */
    private void sendDataToServer() {
        Log.d(TAG, "sendDataToServer");

        // requests queue to be sent to server
        RequestQueue queue = Volley.newRequestQueue(this);

        // format data to be sent to server
        JSONObject plate = formatJSONPlate();

        // checks to make sure JSON is not empty
        if( !(plate.toString().equals("{}") ) ) {
            // new request to be sent out to server
            Log.d(TAG, "create and send JSON POST request");
            JsonObjectRequest jsonRequest = new JsonObjectRequest
                    (Request.Method.POST, Constants.aws_address, plate,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "onResponse: " + response.toString());
                                    // break down JSON response from server, send user to new
                                    // activity if successful registration, or inform of fail
                                    interpretResponse(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error: " + error.getMessage());

                                    // check for server timeout error
                                    if( error.networkResponse == null ) {
                                        if( error.getClass().equals(TimeoutError.class) ) {
                                            Log.d(TAG, "Error: server timeout");

                                            // TODO create server response error Toast
                                        }
                                    } else {
                                        Log.d(TAG, "Error: server problem");

                                        // TODO error Toast
                                    }
                                }
                            });

            // new request added to queue
            queue.add(jsonRequest);
        } else {
            Log.d(TAG, "json variable empty error");

            // TODO create empty JSON error Toast
        }
    }

    /**
     * Interprets the response from the server and informs user of plate
     * registration success/failure
     * @param response the JSON response from a server
     */
    private void interpretResponse(JSONObject response) {
        Log.d(TAG, "interpretResponse");

        //attempt to breakdown server JSON response
        try {
            String plateStatus = response.get("plate_register").toString();
            // TODO make make sure server is returning expected JSON "plate_register"
            // logic checking plate was registered, redirecting user accordingly
            if( plateStatus.equals("success") ) {
                Log.d(TAG, "interpretResponse: success");

                // TODO Toast user about success

                // send user to home activity
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (plateStatus.equals("failure") ) {
                Log.d(TAG, "interpretResponse: failure");

                // clear plate global variables
                Variables.user_plate = "";
                Variables.user_state = "";

                // TODO add Toast explaining to user of failure
                // TODO reset activity or tell user to re-enter plate

            }
        } catch (JSONException je) {
            je.printStackTrace();
            Log.e(TAG, "JSONException error: " + je);

            // TODO add error Toast
        }
    }

    /**
     * @return JSON object to be sent and register a plate to a user,
     * on JSONException error, returns empty object
     */
    private JSONObject formatJSONPlate() {
        Log.d(TAG, "formatJSONRegister data to send to server");

        JSONObject plate = new JSONObject();
        try {
            // server is set to recognize "messageType" in JSON object and
            // process data accordingly
            plate.put("messageType", "plate");
            plate.put("user_id", Variables.user_id);
            plate.put("plate_number", Variables.user_plate);
            plate.put("plate_state", Variables.user_state);
            //TODO TEST that this is how server is expecting JSON

            return plate;
        } catch (JSONException je) {
            je.printStackTrace();
            Log.d(TAG, "JSON format error" + je);
            plate = new JSONObject();
        }
        Log.d(TAG, "formatJSONPlate: " + plate.toString() );
        return plate;
    }
}
