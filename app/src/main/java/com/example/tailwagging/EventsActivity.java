package com.example.tailwagging;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        recyclerView = findViewById(R.id.rvAllEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Event> allEvents = EventStore.getInstance(this).getAllEvents();
        adapter = new EventsAdapter(allEvents);
        recyclerView.setAdapter(adapter);
    }
}