package org.openalpr.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Cameron on 3/4/2016.
 * Listens for notifications sent to device from GCM.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    protected String TAG = "GcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived");
        // get messages from GCM bundle
        String bMessage = data.getString("message");

        // convert bundle data into json string format
        Log.d(TAG, "toJSON: " + bMessage);
        String sMessage = "";
        try {
            // create JSONObject from GCM Bundle
            JSONObject jMessage = new JSONObject(bMessage);
            // create String representation of JSONObject of message to be stored
            sMessage = convertJson(jMessage.get("mid").toString(), jMessage.get("timestamp").toString(),
                    jMessage.get("gpsLon").toString(), jMessage.get("gpsLat").toString(),
                    jMessage.get("message").toString() );
            Log.d(TAG, "convertJson: " + sMessage);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException: " + e);
        }


        // Use method that stores messages to a file.
        // TODO make sure this is storing
        storeMessage(sMessage, this);

        // display notification
        // TODO make sure notification is working
        sendNotification(sMessage);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Log.d(TAG, "sendNotification");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("New PlateScanner Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Will be how a message is broken down and saved on a device
     * @param sMessage string representation of JSON object of message
     * @param context context in which the file is being written
     */
    protected void storeMessage(String sMessage, Context context) {
        Log.d(TAG, "receiveMessage: " + sMessage);

        // add new line character to the end of the message
        sMessage = sMessage + "\n";
        FileOutputStream file;
        // open file to be written to
        try {
            file = context.openFileOutput(Variables.MESSAGE_FILE, Context.MODE_APPEND);
            // write new message to file
            try {
                file.write( sMessage.getBytes() );
                file.close();
                Log.d(TAG, "storeMessage: file.close();");
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e);
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Convert the GCM Bundle data into a string representation of a
     * JSON object for storing purposes
     * @param mid message id
     * @param timestamp timestamp of message
     * @param gpsLon longitude coordinates of incident
     * @param gpsLat latitude coordinates of incident
     * @param message user message
     * @return JSON object of data
     */
    protected String convertJson(String mid, String timestamp, String gpsLon, String gpsLat, String message) {
        Log.d(TAG, "convertJSON");
        String json = "{\"mid\":\""         + mid       + "\"," +
                       "\"timestamp\":\""   + timestamp + "\"," +
                       "\"gps_lon\":\""     + gpsLon    + "\"," +
                       "\"gps_lat\":\""     + gpsLat    + "\"," +
                       "\"message\":\""     + message   + "\"}";
                       // read : 0 or 1
        // TODO add boolean for read/not read
        Log.d(TAG,"convertJSON result: " + json);
        return json;
    }
}
