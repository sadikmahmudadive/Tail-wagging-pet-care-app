package com.example.tailwagging;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cloudinary.android.MediaManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase Offline Persistence
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/");
        db.setPersistenceEnabled(true);
        DatabaseReference rootRef = db.getReference();

        // Keep critical data synced for offline use
        rootRef.child("users").keepSynced(true);
        rootRef.child("pets").keepSynced(true);
        rootRef.child("notifications").keepSynced(true);

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dhm0edatk");
        config.put("api_key", "879315316647413");
        MediaManager.init(this, config);

        createNotificationChannel();
        setupGlobalInteractionTracker();
        subscribeToGlobalTopic();
        startNotificationListener();
    }

    public void startNotificationListener() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference notifRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("notifications").child(uid);
        
        long listenerStartTime = System.currentTimeMillis();

        // Listen for new SYSTEM messages
        notifRef.limitToLast(5).addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                NotificationItem item = snapshot.getValue(NotificationItem.class);
                // Only show if it's a SYSTEM message and was created AFTER we started listening (or very recently)
                if (item != null && "SYSTEM".equals(item.type)) {
                    if (item.serverTimestamp >= listenerStartTime - 5000) { // 5s buffer
                        showSystemNotification(item);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void showSystemNotification(NotificationItem item) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelId = "push_notifications";

        android.content.Intent intent = new android.content.Intent(this, NotificationActivity.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(item.title)
                .setContentText(item.message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void subscribeToGlobalTopic() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("App", "Subscribed to all_users topic");
                    }
                });
    }

    private void setupGlobalInteractionTracker() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {
                // Automatically log which screen the user is viewing
                LogManager.logScreenView(activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityResumed(Activity activity) {}
            @Override
            public void onActivityPaused(Activity activity) {}
            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Channel 1: Event Reminders
            NotificationChannel eventChannel = new NotificationChannel(
                    "event_reminders",
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            eventChannel.setDescription("Notifications for pet care events and tasks");
            manager.createNotificationChannel(eventChannel);

            // Channel 2: Push Alerts (Global Broadcasts)
            NotificationChannel pushChannel = new NotificationChannel(
                    "push_notifications",
                    "Push Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            pushChannel.setDescription("General app updates and alerts");
            manager.createNotificationChannel(pushChannel);
        }
    }
}