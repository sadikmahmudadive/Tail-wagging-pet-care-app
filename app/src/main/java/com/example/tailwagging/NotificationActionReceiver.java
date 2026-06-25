package com.example.tailwagging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotifActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notificationId = intent.getIntExtra("notificationId", -1);
        String firebaseNotifId = intent.getStringExtra("firebaseNotifId");

        if (notificationId != -1) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }

        if ("ACTION_DISMISS".equals(action)) {
            Log.d(TAG, "Notification dismissed");
            if (firebaseNotifId != null) {
                markAsRead(firebaseNotifId);
            }
        } else if ("ACTION_MARK_DONE".equals(action)) {
            Log.d(TAG, "Notification marked as done");
            if (firebaseNotifId != null) {
                markAsRead(firebaseNotifId);
            }
            // Future: Could also update the Event status if linked
        }
    }

    private void markAsRead(String firebaseNotifId) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        dbRef.child("notifications").child(uid).child(firebaseNotifId).child("read").setValue(true);
    }
}
