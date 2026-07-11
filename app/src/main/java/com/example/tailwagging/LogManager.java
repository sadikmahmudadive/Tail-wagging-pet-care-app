package com.example.tailwagging;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogManager {
    private static final String LOGS_NODE = "user_interaction_logs";
    private static final DatabaseReference dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference(LOGS_NODE);

    public static void logScreenView(String screenName) {
        logAction("Screen View", screenName);
    }

    public static void logButtonClick(String buttonName) {
        logAction("Button Click", buttonName);
    }

    public static void logAiAction(String action, String detail) {
        logAction("AI Interaction: " + action, detail);
    }

    public static void logAction(String action, String detail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "Anonymous";
        String userName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Unknown";

        String logId = dbRef.push().getKey();
        if (logId != null) {
            UserLog log = new UserLog(logId, userId, userName, action, detail);
            dbRef.child(logId).setValue(log);
        }
    }
}
