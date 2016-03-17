package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by Anthony Brignano on 2/17/16.
 * <p/>
 * LoginActivity: Allows users to login to an existing account
 * (associated view activity_login)
 * <p/>
 * - redirectToRegister(View): redirects user to RegisterActivity.java (activity_register.xml) on click
 * - Login(View): parses user credentials entered into the views TextFields
 * - onActivityResult(int, int, Intent):
 * - onCreate(Bundle)
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context context;
    protected GoogleCloudMessaging gcm = null;
    protected InstanceID iid = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();

        iid = InstanceID.getInstance(context);
        gcm = GoogleCloudMessaging.getInstance(context);
        // Register an InstanceID
        Log.v(TAG, "GCM_REGISTER BEFORE " + Constants.INST_ID);
        Constants.INST_ID = iid.getId();
        Log.v(TAG, "GCM_REGISTER AFTER " + Constants.INST_ID);

        // Get token
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                Log.v(TAG, "GCM_TOKE BEFORE");
                String authorizedEntity = Constants.PROJECT_ID;
                String scope = "GCM";
                try {
                    Log.v(TAG, "GCM_TOKE BEFORE_IN_TRY");
                    Constants.REG_TOKEN = InstanceID.getInstance(context).getToken(authorizedEntity, scope);
                    msg = Constants.REG_TOKEN;
                    Log.v(TAG, "GCM_TOKE AFTER_IN_TRY, Reg_Token: " + Constants.REG_TOKEN);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(TAG, "GCM_TOKE FAIL FAIL FAIL FAIL");
                }
                Log.v(TAG, "GCM_TOKE AFTER");

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
                Log.v(TAG, "GCM_TOKE: LEAVING LEAVING LEAVING");
            }
        }.execute(null, null, null);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void Login(View view) {
        /**
         * FORMAT: "variable_name - function"
         *
         * u - EditText variable for text input in username field
         * p - EditText variable for text input in password field
         * username - String variable for text in username field
         * password - String variable for text in password field
         *
         * */

        EditText u = (EditText) findViewById(R.id.username);
        EditText p = (EditText) findViewById(R.id.password);
        String username = u.getText().toString();
        String password = p.getText().toString();
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        // Logic needs to be added to this variable based on the database interaction(s)
        Boolean loginSuccess = false;


        // changed for gcm test
        if (loginSuccess) {
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
        }
    }

    public void redirectToRegister(View view) {
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Intent scanIntent = new Intent(this, ScanPlate.class);
        String platePath = intent.getStringExtra("picture");

        scanIntent.putExtra("platepicture", platePath);

        Log.d(TAG, "Starting ScanPlate.class");
        Log.d(TAG, "Image file path: " + platePath);

        startActivity(scanIntent);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.openalpr.app/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.openalpr.app/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
