package org.openalpr.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by Cameron on 3/4/2016.
 *
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "IntentService(Activity)";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final InstanceID instanceID = InstanceID.getInstance(this);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String token ="";
                try {
                    // Initially this call goes out to the network to retrieve the
                    // token, subsequent calls are local.
                    token = instanceID.getToken(Variables.SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]
                    Log.i(TAG, "GCM Registration Token: " + token);
                    Variables.gcm_user_id = token;

                } catch (Exception e) {
                    Log.d(TAG, "Failed to complete token refresh", e);
                    // If an exception happens while fetching the new token or
                    // updating our registration data on a third-party server, this
                    // ensures that we'll attempt the update at a later time.
                    sharedPreferences.edit().putBoolean("sentTokenToServer", false).apply();
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.d(TAG, "onPostExecute");
            }
        };

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent("registrationComplete");
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
