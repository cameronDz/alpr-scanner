package org.openalpr.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by Travis on 4/6/16.
 */
public class MessageItem implements Parcelable {

    private String mid;
    private String timestamp;
    private String gpsLon;
    private String gpsLat;
    private String message;
    private boolean read;
    private boolean replied;

    public MessageItem() {}

    public MessageItem(Parcel input) {
        mid = input.readString();
        timestamp = input.readString();
        gpsLon = input.readString();
        gpsLat = input.readString();
        message = input.readString();

        if (input.readInt() == 0) {
            read = false;
        } else {
            read = true;
        }

        if (input.readInt() == 0) {
            replied = false;
        } else {
            replied = true;
        }
    }

    public void setMid(String mid) {this.mid = mid;}

    public String getMid() { return mid;}

    public void setTimestamp(String timestamp) { this.timestamp = timestamp;}

    public String getTimestamp() {return timestamp;}

    public void setGpsLon(String gpsLon) {this.gpsLon = gpsLon;}

    public String getGpsLon() {return gpsLon;}

    public void setGpsLat(String gpsLat) {this.gpsLat = gpsLat;}

    public String getGpsLat() { return gpsLat;}

    public void setMessage(String message) { this.message = message;}

    public String getMessage() { return message;}

    public void setReadFalse() {
        this.read = false;
    }

    public void setReadTrue() {
        this.read = true;
    }

    public boolean getReadStatus() { return this.read; }

    public boolean getReplyStatus() { return this.replied; }

    public void setReplyTrue() {
        this.replied = true;
    }

    public void setReplyFalse() {
        this.replied = false;
    }

    public String messageToString() {
        String stringMessageItem;

        stringMessageItem = "{\"mid\":\""         + this.getMid()       + "\"," +
                "\"timestamp\":\""   + this.getTimestamp() + "\"," +
                "\"gps_lon\":\""     + this.getGpsLon()    + "\"," +
                "\"gps_lat\":\""     + this.getGpsLat()    + "\"," +
                "\"message\":\""     + this.getMessage()   + "\"," +
                "\"read\":\""        + this.getReadStatus()+ "\"," +
                "\"replied\":\""    + this.getReplyStatus()+ "\"}";

        return stringMessageItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // these must be retrieved in same order else the wrong value will be retrieved

        dest.writeString(mid);
        dest.writeString(timestamp);
        dest.writeString(gpsLon);
        dest.writeString(gpsLat);
        dest.writeString(message);

        // No method for writing boolean will have to find value and send int
        if(getReadStatus() == false ) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
        }

        if(getReplyStatus() == false) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
        }

    }

    public static final Parcelable.Creator<MessageItem> CREATOR
            = new Parcelable.Creator<MessageItem>() {
        public MessageItem createFromParcel(Parcel in) {
            Log.d("create parcel", "message item");

            return new MessageItem(in);
        }

        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };
}