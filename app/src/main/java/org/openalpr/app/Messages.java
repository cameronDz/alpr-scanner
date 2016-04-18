package org.openalpr.app;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Travis on 4/6/16.
 */
public class Messages implements Serializable {

    private final String TAG = "Messages.Class";

    private List<MessageItem> messages;

    public Messages() {
        messages = new ArrayList<MessageItem>();
    }


    public List<MessageItem> getMessages() {
        return messages;
    }

  //  public void addMessageItem(MessageItem message) { messages.add(message);}

}
