package com.example.tailwagging;

public class UserLog {
    public String logId;
    public String userId;
    public String userName;
    public String action; // e.g., "Screen View", "Button Click", "AI Scan"
    public String detail; // e.g., "Main Dashboard", "Place Order Button", "Golden Retriever Scan"
    public long timestamp;

    public UserLog() {}

    public UserLog(String logId, String userId, String userName, String action, String detail) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.detail = detail;
        this.timestamp = System.currentTimeMillis();
    }
}
