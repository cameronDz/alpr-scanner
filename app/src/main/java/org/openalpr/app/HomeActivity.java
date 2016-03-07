package org.openalpr.app;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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
 * version@(7.3.2016) user@(cameronDz) Set welcome message to include Variable.username String
 */

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Locate and set TextView that displays welcome message
        TextView welcome_text =(TextView)findViewById(R.id.text_welcome);
        String message = "Welcome " + Variables.username + "!";
        welcome_text.setText(message);
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
