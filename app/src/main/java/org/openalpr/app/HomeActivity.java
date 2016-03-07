package org.openalpr.app;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Anthony Brignano on 2/26/16.
 *
 * HomeActivity: Allows for basic navigation betgithween application views
 *      (associated view activity_home)
 *
 *      - redirectToInbox(View): redirects user to InboxActivity.java (activity_inbox.xml) on click
 *      - redirectToCamera(View): redirects user to CameraActivity.java (activity_camera.xml) on click
 *      - onCreate(Bundle)
 *
 */

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private Context context;
    protected GoogleCloudMessaging gcm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = getApplicationContext();

        gcm = GoogleCloudMessaging.getInstance(context);

        // Locate TextView that displays welcome message
        TextView welcome_text =(TextView)findViewById(R.id.text_welcome);

        // the username variable needs to be set to the User's current username
        String username = "{username}";
        String message = "Welcome " + username + "!";

        // sets the message to be displayed inside the TextView
        welcome_text.setText(message);


        //gcm sends hi message to server
        Log.v("GCM_PRINT gcm: ", gcm.toString());
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "Sent message";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "SAY_HELLO");
                    data.putString("my_action2", "SAY_HELLO2");
                    data.putString("my_action3", "SAY_HELLO3");
                    data.putString("my_action4", "SAY_HELLO4");
                    data.putString("my_action5", "SAY_HELLO5");
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
    }

    public void redirectToInbox(View view) {
        Intent intent = new Intent(this, InboxActivity.class);
        startActivity(intent);
    }
    public void redirectToCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


}