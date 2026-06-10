package com.example.tailwagging;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventStore {
    private static final String PREFS_NAME = "event_store";
    private static final String EVENTS_KEY = "events";
    private static EventStore instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<Event> events;

    private EventStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
        loadEvents();
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(formatter));
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString(), formatter);
        }
    }

    private static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

        @Override
        public JsonElement serialize(LocalTime time, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(time.format(formatter));
        }

        @Override
        public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalTime.parse(json.getAsString(), formatter);
        }
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
            try {
                Type listType = new TypeToken<ArrayList<Event>>() {}.getType();
                events = gson.fromJson(json, listType);
                if (events == null) events = new ArrayList<>();
            } catch (Exception e) {
                // If parsing fails (e.g. format mismatch after update), start fresh
                events = new ArrayList<>();
                saveEvents();
            }
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

    public void removeEventsByPetId(String petId) {
        if (petId == null) return;
        boolean removed = events.removeIf(e -> petId.equals(e.petId));
        if (removed) {
            saveEvents();
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