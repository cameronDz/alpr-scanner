package org.openalpr.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Anthony Brignano on 2/14/16.
 *
 * VerifyPlateActivity: For verification of the image recognition
 *      (associated view: activity_verify)
 *
 *      - verifyPlate(View): redirects to MessageActivity (on button click)
 *      - onCreate(Bundle)
 *
 * TO DO:
 *  - Create variables for each button so their text can be easily accessed and manipulated
 *  - Create method: store all OpenALPR results in an array
 *  - Display A[0-2] (in order by OpenALPR's confidence interval)
 *  - Create method: increase display index A[i] by 2 (to be called when Display More Plates button is clicked)
 *
 */

public class VerifyPlateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

    }

    /** Called when the user clicks the button to verify the plate */
    public void verifyPlate(View view) {
        // Do something in response to button

        Button plate_button = (Button)view;
        String plate = plate_button.getText().toString();


        // Open next activity view in application flow
        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
    }


}