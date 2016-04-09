package org.openalpr.app;

import java.io.Serializable;

/**
 * Created by Travis on 4/6/16.
 */
public class MessageItem implements Serializable {

    private String mid;
    private String timestamp;
    private String gpsLon;
    private String gpsLat;
    private String message;
    private boolean read;
    private boolean replied;

    public MessageItem() {}

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
}