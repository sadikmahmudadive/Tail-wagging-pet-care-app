package com.example.tailwagging;

import java.io.Serializable;

public class NotificationItem implements Serializable {
    public String id;
    public String title;
    public String message;
    public String timestamp;
    public boolean isRead;
    public String type; // e.g., "ALARM", "INFO", "SYSTEM"
    public long serverTimestamp;

    public NotificationItem() {}

    public NotificationItem(String id, String title, String message, String timestamp, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
        this.type = type;
        this.serverTimestamp = System.currentTimeMillis();
    }
}
