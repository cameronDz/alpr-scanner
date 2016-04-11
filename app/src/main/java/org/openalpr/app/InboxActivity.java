package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Anthony Brignano on 2/19/16.
 *
 * InboxActivity: For bulk message retrieval/history
 *      (associated view activity_inbox)
 *
 *      - onCreate(Bundle)
 *
 * TO DO:
 *  - add CustomView for messages
 *
 *  Edit by Travis Moretz on 4/9/19
 *
 *  Gets messages from file on device
 *  Parses them into messageItem objects and displays on screen in custom listView with newest to top.
 *  Users can view message on map by clicking the message item or delete the message by clicking
 *  the delete button.  Will update the file if messages are deleted and rewrite file
 */

public class InboxActivity extends AppCompatActivity {

    private static String TAG = "InboxActivity";

    private Context context;

    private final int MID = 0;
    private final int TIMESTAMP = 1;
    private final int GPS_LON = 2;
    private final int GPS_LAT = 3;
    private final int MESSAGE = 4;

    // Messages for testing

    private String test1 = "{\"mid\": \"0101\",\"timestamp\":\"December 21, 2012\",\"gps_lon\":\"-72.515108\",\"gps_lat\":\"41.793851\",\"message\":\"1\"}";

    private String test2 = "{\"mid\":\"0111\",\"timestamp\":\"December 22, 2012\",\"gps_lon\":\"-72.76655\",\"gps_lat\":\"41.690668\",\"message\":\"2\"}";

    private String test3 = "{\"mid\":\"08931\",\"timestamp\":\"December 23, 2012\",\"gps_lon\":\".11\",\"gps_lat\":\".34\",\"message\":\"3\"}";

    private String test4 = "{\"mid\":\"0321\",\"timestamp\":\"December 24, 2012\",\"gps_lon\":\".76\",\"gps_lat\":\".67\",\"message\":\"4\"}]";

    private String test5 = "{\"mid\":\"0321\",\"timestamp\":\"December 24, 2012\",\"gps_lon\":\".76\",\"gps_lat\":\".67\",\"message\":\"5\"}]";

    private String test6 = "{\"mid\":\"0321\",\"timestamp\":\"December 24, 2012\",\"gps_lon\":\".76\",\"gps_lat\":\".67\",\"message\":\"6\"}]";

    private ArrayList<MessageItem> messages;

    private ArrayAdapter<MessageItem> adapter;

    private ArrayList<String> messageStringList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        context = this;
        Log.d(TAG, "In onCreate");
//        deleteFile(Variables.MESSAGE_FILE);
//        storeMessage(test1, this.context);
//        storeMessage(test2, this.context);
//        storeMessage(test3, this.context);
//        storeMessage(test4, this.context);
//        storeMessage(test5, this.context);
//        storeMessage(test6, this.context);
//        clearAndStoreMessage(test1, this.context);


    }

    protected void onStart() {
        super.onStart();
        Log.d(TAG, "In onStart");

        // Get the arrayList of strings saved to file
        messageStringList = Variables.messages(context);

        Log.d(TAG, "message list size = " + messageStringList.size());

        // create arrayList of message objects from the strings
        messages = new ArrayList<MessageItem>();
        loadMessages(messageStringList);


        // load them on the screen
        populateListView();

        // click listener for loading message view
        registerClickCallback();
    }

    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "In onStop");
        reverseAndSaveMessages();
        Log.d(TAG, "messagesSize Before: " + messages.size());
        messages.clear();
        Log.d(TAG, "messagesSize After: " + messages.size());
        Log.d(TAG, "MessagesListSize: Before: " + messageStringList.size());
        messageStringList.clear();
        Log.d(TAG, "MessagesListSize: After: " + messageStringList.size());

    }

    // Click handler for touching message item
    private void registerClickCallback() {
 //       Log.d(TAG, "In registerClickCallBack");
        ListView listView = (ListView) findViewById(R.id.inbox_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                MessageItem clickedMessage = messages.get(position);

                clickedMessage.setReadTrue();
                //   Toast.makeText(InboxActivity.this, "list item: " + position, Toast.LENGTH_SHORT).show();
                redirectToMessageView(viewClicked, clickedMessage);
                adapter.notifyDataSetChanged();
            }

        });

    }

    // set up for filling screen
    private void populateListView() {
 //       Log.d(TAG, "In populateView");
        adapter = new MyListAdaptor();
        ListView listView = (ListView) findViewById(R.id.inbox_list);
        listView.setAdapter(adapter);

    }

    // customer array adaptor for MessageItmes
    private class MyListAdaptor extends ArrayAdapter<MessageItem> {
        public MyListAdaptor() {
            super(InboxActivity.this, R.layout.message_list_item, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent ) {
//            Log.d(TAG, "In getView");
            final int index = position;
            // get the message
            MessageItem currentMessageItem = messages.get(position);


            // Get a view to work with, if null create and inflate the view
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.message_list_item, parent, false);
            }

            // Set button
            Button button = (Button) itemView.findViewById(R.id.item_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "delete #: " + index, Toast.LENGTH_SHORT).show();
                    messages.remove(index);
                    adapter.notifyDataSetChanged();
                }
            });

            // fill text
            TextView textView = (TextView) itemView.findViewById(R.id.item_timestamp);
            textView.setText(currentMessageItem.getTimestamp());


            // fill the view with correct image
            ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);

            switch (currentMessageItem.getMessage()) {
                case "1":
                    imageView.setImageResource(R.drawable.mad);
                    break;
                case "2":
                    imageView.setImageResource(R.drawable.thankyou);
                    break;
                case "3":
                    imageView.setImageResource(R.drawable.slowdown);
                    break;
                case "4":
                    imageView.setImageResource(R.drawable.no_parking);
                    break;
                case "5":
                    imageView.setImageResource(R.drawable.cellphone);
                    break;
                case "6":
                    imageView.setImageResource(R.drawable.mechanic);
                    break;
            }
            return itemView;
        }

    }


    // fill the arraylist for view
    private void loadMessages(ArrayList<String> messageStringList) {
  //      Log.d(TAG, "In loadMessages");

        for(int i = 0; i < messageStringList.size(); i++) {
//            Log.d(TAG, "MESS " + i + ": " + messageStringList.get(i).toString());
            String[] mess = messBreakDown(messageStringList.get(i));
            MessageItem messageItem = new MessageItem();

//            Log.d(TAG, "mid: " + mess[MID]);
            messageItem.setMid(mess[MID]);
//            Log.d(TAG, "MID: " + messageItem.getMid());

//            Log.d(TAG, "timestamp: " + mess[TIMESTAMP]);
            messageItem.setTimestamp(mess[TIMESTAMP]);
//            Log.d(TAG, "TIMESTAMP" + messageItem.getTimestamp());

//            Log.d(TAG, "longitude: " + mess[GPS_LON]);
            messageItem.setGpsLon(mess[GPS_LON]);
//            Log.d(TAG, "LONGITUDE: " + messageItem.getGpsLon());

//            Log.d(TAG, "latitude: " + mess[GPS_LAT]);
            messageItem.setGpsLat(mess[GPS_LAT]);
//            Log.d(TAG, "LATITUDE: " + messageItem.getGpsLat());

//            Log.d(TAG, "message: " + mess[MESSAGE]);
            messageItem.setMessage(mess[MESSAGE]);
//            Log.d(TAG, "MESSAGE: " + messageItem.getMessage());

            messageItem.setReadFalse();
//            Log.d(TAG, "READ: " + messageItem.getReadStatus());


//            Log.d(TAG, "MESSAGE ITEM toSTRING: " + messageItem.messageToString());

            messages.add(messageItem);
        }

        messageStringList.clear();
        // news messages to top of list
        Collections.reverse(messages);

    }

    public void redirectToMessageView(View view, MessageItem clickedMessage) {
        Intent intent = new Intent(context, MessageViewActivity.class);

        // a parcelable version of clickedMessage to send to message view
        intent.putExtra("messageItem", clickedMessage);

        startActivity(intent);
    }


    /**
     * TODO
     *
     * Make a function that passes all messages to map and pins them all
     * @param view
     */
    public void redirectToMap(View view) {
        Intent intent = new Intent(context, MapActivity.class);
        startActivity(intent);
    }

    // When saving messages they need to be reversed in to be put back in file, because
    // new messages will be appended to file when they are sent from server
    public void reverseAndSaveMessages() {
        Log.d(TAG, "In reverseAndSaveMessages");

        // delete file
        deleteFile(Variables.MESSAGE_FILE);

        if (!messages.isEmpty()) {
            // reverse the order - because the file has new messages appended to end
            Collections.reverse(messages);
        // string-a-fi all messages back to file
             for(int i = 0; i < messages.size(); i++) {
                 Log.d(TAG, "Call store and same messages");
                 storeMessage(messages.get(i).messageToString(), this.context);
            }
        }
    }

    // Need a method to clear the file of all messages, before they are saved back to it.
    // Allows for user to update the list of messaged by deleting messages
    protected void clearAndStoreMessage(String sMessage, Context context) {
     //   Log.d(TAG, "receiveMessage: " + sMessage);
        Log.d(TAG, "In clearAndStoreMessage");

        // add new line character to the end of the message
        sMessage = sMessage + "\n";
        FileOutputStream file;
        // open file to be written to
        try {
            file = context.openFileOutput(Variables.MESSAGE_FILE,0);
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

    // Method for appending messages without overwriting
    protected void storeMessage(String sMessage, Context context) {
      //  Log.d(TAG, "receiveMessage: " + sMessage);
        Log.d(TAG, "In storeMessage");

        // add new line character to the end of the message
        sMessage = sMessage + "\n";
        FileOutputStream file;
        // open file to be written to
        try {
            file = context.openFileOutput(Variables.MESSAGE_FILE,context.MODE_APPEND);
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
   //     Log.d(TAG, "In messBreakDown");
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
