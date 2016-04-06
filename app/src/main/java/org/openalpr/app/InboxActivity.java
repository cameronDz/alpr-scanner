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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
 */

public class InboxActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "InboxActivity";

    private Context context;

    private String testJSON = "[{\"mid\": \"0101\" ,\"timestamp\":December212012,\"gps_lon\":.01,\"gps_lat\":.02,\"message\":1},{\"mid\":\"0111\",\"timestamp\":December222012,\"gps_lon\":.05,\"gps_lat\":.12,\"message\":2},{\"mid\":\"08931\",\"timestamp\":December232012,\"gps_lon\":.11,\"gps_lat\":.34,\"message\":4},{\"mid\":\"0321\",\"timestamp\":December242012,\"gps_lon\":.76,\"gps_lat\":.67,\"message\":3}]";

    private ArrayList<MessageItem> messages;

    private ArrayAdapter<MessageItem> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        context = this;

        try {

            JSONArray jsonArray = new JSONArray(testJSON);
            loadMessages(jsonArray);

        } catch(Exception e) {
            Log.e(TAG, "Exception parsing JSON result", e);
        }


        ListView messageListView = (ListView) findViewById(R.id.inbox_list);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(this);

    }

    private void loadMessages(JSONArray jsonArray) throws JSONException {

        messages = new ArrayList<MessageItem>();
        Log.d(TAG, String.valueOf(jsonArray.length()));


        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Log.d(TAG, String.valueOf(jsonObject.getInt("mid")));
            Log.d(TAG, jsonObject.getString("message"));
            Log.d(TAG, jsonObject.getString("timestamp"));


        }
        adapter = new ArrayAdapter<MessageItem>(
                this,
                R.layout.message_list_item,
                R.id.item_text,
                messages
        );
    }


    /**
     * when message is clicked open up the message view
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String item = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "open message " + item);
        Log.d(TAG, "Position " + position);

        ImageView imageView = (ImageView) view.findViewById(R.id.mail_picture);
        imageView.setVisibility(View.INVISIBLE);

    }

    public void redirectToMessageView(View view) {
        Intent intent = new Intent(context, MessageViewActivity.class);
        startActivity(intent);
    }

    public void redirectToMap(View view) {
        Intent intent = new Intent(context, MapActivity.class);
        startActivity(intent);
    }


    protected void getMessage(String sMessage, Context context) {

    }

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
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e);
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
        }
    }

}







//        getView(position, messageListView, parent);


// open the message view
//        Intent intent = new Intent(this, MessageViewActivity.class);

//    public View getView (int position, View convertView, ViewGroup parent){
//
//        Log.d(TAG, "Position : " + position + " " + convertView + " "  + parent);
//        if( convertView == null ){
//            //We must create a View:
//            LayoutInflater inflater = (LayoutInflater)context.getSystemService
//                    (Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.message_list_item, parent, false);
//        }
//        //Here we can do changes to the convertView, such as set a text on a TextView
//        //or an image on an ImageView.
////        ImageView imageView = (ImageView) findViewById(R.id.mail_picture);
////        imageView.setVisibility(View.INVISIBLE);
//        return convertView;
//    }
