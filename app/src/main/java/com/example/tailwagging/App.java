package com.example.tailwagging;

import android.app.Application;

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
    }
}