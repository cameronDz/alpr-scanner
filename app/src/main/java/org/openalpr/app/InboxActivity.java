package org.openalpr.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

public class InboxActivity extends AppCompatActivity{

    private String TAG = "InboxActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        context = this;
    }

    public void redirectToMessageView(View view) {
        Intent intent = new Intent(context, MessageViewActivity.class);
        startActivity(intent);
    }

    public void redirectToMap(View view) {
        Intent intent = new Intent(context, MapActivity.class);
        startActivity(intent);
    }

}