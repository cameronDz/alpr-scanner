package org.openalpr.app;

/**
 * Created by Cameron on 3/7/2016.
 *
 * Used as a hack for MVP 2 to start variables that are going to be
 * persisted over multiple views then sent upstream through GCM
 */
public class Variables {
    //registering user data
    protected static String username = "";
    protected static String password = "";
    protected static String user_plate = "";
    protected static String user_state = "";

    // sending message data
    protected static String plate_to = "";
    protected static String state_to = "";
    protected static String gps_lat = "";
    protected static String gps_long = "";

    private Variables() {
        // empty
    }
}
