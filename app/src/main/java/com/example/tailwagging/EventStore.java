package com.example.tailwagging;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Objects;

public class EventStore {
    private static final String PREFS_NAME = "event_store";
    private static final String EVENTS_KEY_PREFIX = "events_";
    private static EventStore instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final String userId;
    private List<Event> events;

    private EventStore(Context context, String userId) {
        this.userId = userId;
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
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) currentUid = "guest"; 
        
        if (instance == null || !Objects.equals(instance.userId, currentUid)) {
            instance = new EventStore(context, currentUid);
        }
        return instance;
    }

    private void loadEvents() {
        String json = prefs.getString(EVENTS_KEY_PREFIX + userId, null);
        if (json != null) {
            try {
                Type listType = new TypeToken<ArrayList<Event>>() {}.getType();
                events = gson.fromJson(json, listType);
                if (events == null) events = new ArrayList<>();
            } catch (Exception e) {
                events = new ArrayList<>();
                saveEvents();
            }
        } else {
            events = new ArrayList<>();
        }
    }

    public void saveEvents() {
        String json = gson.toJson(events);
        prefs.edit().putString(EVENTS_KEY_PREFIX + userId, json).apply();
    }

    public void addEvent(Event event) {
        events.add(event);
        saveEvents();
    }

    public void deleteEvent(Event event) {
        if (events.remove(event)) {
            saveEvents();
        }
    }

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

    public List<Event> getUpcomingEvents(LocalDate fromDate) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.date != null && !e.date.isBefore(fromDate)) {
                result.add(e);
            }
        }
        result.sort((e1, e2) -> {
            int dateComp = e1.date.compareTo(e2.date);
            if (dateComp != 0) return dateComp;
            return e1.fromTime.compareTo(e2.fromTime);
        });
        return result;
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