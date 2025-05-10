package com.example.tailwagging;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventStore {
    private static final String PREFS_NAME = "event_store";
    private static final String EVENTS_KEY = "events";
    private static EventStore instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private List<Event> events;

    private EventStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadEvents();
    }

    public static synchronized EventStore getInstance(Context context) {
        if (instance == null) {
            instance = new EventStore(context);
        }
        return instance;
    }

    private void loadEvents() {
        String json = prefs.getString(EVENTS_KEY, null);
        if (json != null) {
            Type listType = new TypeToken<ArrayList<Event>>() {}.getType();
            events = gson.fromJson(json, listType);
            if (events == null) events = new ArrayList<>();
        } else {
            events = new ArrayList<>();
        }
    }

    private void saveEvents() {
        String json = gson.toJson(events);
        prefs.edit().putString(EVENTS_KEY, json).apply();
    }

    public void addEvent(Event event) {
        events.add(event);
        saveEvents();
    }

    // Remove event by event object (new method for deletion)
    public void deleteEvent(Event event) {
        if (events.remove(event)) {
            saveEvents();
        }
    }

    // Remove event by id (existing, for compatibility)
    public void removeEvent(int eventId) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id == eventId) {
                events.remove(i);
                saveEvents();
                break;
            }
        }
    }

    public List<Event> getEventsForDate(LocalDate date) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.date != null && e.date.equals(date)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}