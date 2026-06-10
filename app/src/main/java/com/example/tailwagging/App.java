package com.example.tailwagging;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dhm0edatk");
        config.put("api_key", "879315316647413");
        MediaManager.init(this, config);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "event_reminders",
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for pet care events and tasks");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}