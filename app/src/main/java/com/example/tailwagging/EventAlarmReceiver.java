package com.example.tailwagging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;

public class EventAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "EventAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received!");
        String userId = intent.getStringExtra("userId");
        String currentUid = FirebaseAuth.getInstance().getUid();

        Log.d(TAG, "Event userId: " + userId + ", Current UID: " + currentUid);

        // Only show notification if the user who created it is still logged in
        if (userId != null && !userId.equals(currentUid)) {
            Log.w(TAG, "User ID mismatch. Skipping notification.");
            return;
        }

        String title = intent.getStringExtra("title");
        String category = intent.getStringExtra("category");
        int eventId = intent.getIntExtra("id", 0);

        Intent resultIntent = new Intent(context, EventsActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, eventId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "event_reminders";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle("Event Reminder: " + title)
                .setContentText("Category: " + category)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(eventId, builder.build());

        // Save to Notification Center in Firebase
        saveNotificationToCenter(currentUid, title, category);
    }

    private void saveNotificationToCenter(String userId, String title, String category) {
        if (userId == null) return;
        
        com.google.firebase.database.DatabaseReference dbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        String notifId = dbRef.child("notifications").child(userId).push().getKey();
        
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("hh:mm a").format(java.time.LocalTime.now());
        NotificationItem item = new NotificationItem(notifId, "Reminder: " + title, "Category: " + category, timestamp, "ALARM");
        
        if (notifId != null) {
            dbRef.child("notifications").child(userId).child(notifId).setValue(item);
        }
    }
}