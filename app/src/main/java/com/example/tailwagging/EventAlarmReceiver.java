package com.example.tailwagging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;

public class EventAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String userId = intent.getStringExtra("userId");
        String currentUid = FirebaseAuth.getInstance().getUid();

        // Only show notification if the user who created it is still logged in
        if (userId != null && !userId.equals(currentUid)) {
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
    }
}