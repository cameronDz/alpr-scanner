package org.openalpr.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Travis on 4/6/16.
 */
public class Messages implements Serializable {

    private List<MessageItem> messages;

    public Messages() {
        messages = new ArrayList<MessageItem>();
    }


    public List<MessageItem> getMessages() {
        return messages;
    }

    public void addMessageItem(MessageItem message) { messages.add(message);}
}
