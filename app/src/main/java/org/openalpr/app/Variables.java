package org.openalpr.app;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Cameron on 3/7/2016.
 * Stores variables and constants used in the app to send and receive
 * data from the server.
 *
 * date@(23.03.2016) @editor(cameronDz)
 * Added several methods that will be used in breaking down gcm push
 * notifications containing data. They may be moved, and the key names
 * may be changed, but they represent the algorithm of how the data
 * will be processed.
 */
public class Variables {
    // TODO remove once writeData() method is moved to different class
    // put activity in TAG for easier Log searching
    protected static String TAG = "Variables(Activity)";

    // constants used to register with GCM and make HTTP POST requests
    protected static final String APPLICATION_ID = "org.openalpr.app";
    protected static final String AWS_ADDRESS = "http://107.21.62.238";
    protected static final String SENDER_ID = "938965101228";
    // file name that holds messages sent to user locally
    protected static final String MESSAGE_FILE = "messages";

    // registering user data
    protected static String username = "";
    protected static String password = "";
    protected static String user_plate = "";
    protected static String user_state = "";
    // TODO get GCM ID through AsyncTask
    protected static String gcm_user_id = "";
    protected static String gcm_inst_id = "";
    protected static int user_id = 0;

    // sending message data
    protected static String plate_to = "";
    protected static String state_to = "";
    protected static String message = "";
    protected static String time = "";
    protected static double gps_lat = 0;
    protected static double gps_long = 0;

    private Variables() {
        // empty
    }

    // TODO move to appropriate class (GCM related)
    /**
     * Will be how a message is broken down and saved on a device
     * @param sMessage string representation of JSON object of message
     * @param context context in which the file is being written
     */
    protected static void receiveMessage(String sMessage, Context context) {
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
     * @param context activity context which is pulling the data
     * @return a array of strings that represent messages store messages
     *         File. Each String in the array can be converted from it's
     *         JSON representation into the key/values using the
     *         messBreakDown() below this method.
     */
    protected static ArrayList<String> messages(Context context) {
        Log.d(TAG, "retrieveMessage");
        // ArrayList of strings representing JSON messages
        ArrayList<String> s = new ArrayList<>();
        FileInputStream in;

        try {
            in = context.openFileInput(Variables.MESSAGE_FILE);
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);

            // throw exception if unable to read
            if( !br.ready() ) {
                in.close();
                throw new FileNotFoundException();
            }

            // loop through file until there is no next line
            int i = 0;
            String line = br.readLine();
            while( line != null ) {
                s.add(line);
                line = br.readLine();
                i++;
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e);
            e.printStackTrace();
        }

        return s;
    }

    /**
     * Breaks down a stored string representation of a JSON Object of a message
     * into the components of the message
     * @param message the stored string
     * @return s[0] = mid = the unique message id
     *         s[1] = timestamp = time message was sent
     *         s[2] = gps_lon = longitude gps coordinate
     *         s[3] = gps_lat = latitude gps coordinate
     *         s[4] = message = actually message that was sent
     */
    protected static String[] messBreakDown(String message) {
        Log.d(TAG, "messageBreakDown: " + message);
        String[] s = new String[5];

        // attempt to break string into components
        try {
            JSONObject json = new JSONObject(message);

            // TODO check that these are the key/value being sent in messages
            s[0] = json.get("mid").toString();
            s[1] = json.get("timestamp").toString();
            s[2] = json.get("gps_lon").toString();
            s[3] = json.get("gps_lat").toString();
            s[4] = json.get("message").toString();
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
            e.printStackTrace();
        }

        return s;
    }
}
