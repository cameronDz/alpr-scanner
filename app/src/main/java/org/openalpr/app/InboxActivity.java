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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        context = this;


    // dummy array list of messages for testing

        ArrayList<String> messageList = new ArrayList<>();
        messageList.add("first");
        messageList.add("second");
        messageList.add("third");
        messageList.add("fourth");
        messageList.add("fifth");
        messageList.add("sixth");
        messageList.add("seven");
        messageList.add("eight");
        messageList.add("nine");
        messageList.add("ten");
        messageList.add("11");
        messageList.add("45");
        messageList.add("6hadk");
        messageList.add("testing");
        messageList.add("hello");
        messageList.add("dog");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.message_list_item,
                R.id.item_text,
                messageList
        );

        ListView messageListView = (ListView) findViewById(R.id.inbox_list);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(this);

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
        ListView messageListView = (ListView) findViewById(R.id.inbox_list);
        String messageItem = messageListView.getItemAtPosition(position).toString();
        Toast.makeText(this, messageItem, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "open message " + messageItem);
        Log.d(TAG, "Position " + position);


//        getView(position, messageListView, parent);


        // open the message view
//        Intent intent = new Intent(this, MessageViewActivity.class);
    }

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



}