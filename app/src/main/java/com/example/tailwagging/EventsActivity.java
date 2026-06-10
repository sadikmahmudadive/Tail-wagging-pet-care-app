package com.example.tailwagging;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventsActivity extends AppCompatActivity implements TodayEventAdapter.OnEventChangedListener {
    private RecyclerView recyclerView;
    private TodayEventAdapter adapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        recyclerView = findViewById(R.id.rvAllEvents);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Event> allEvents = EventStore.getInstance(this).getAllEvents();
        adapter = new TodayEventAdapter(this, allEvents, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEventDeleted() {
        // Handle if needed, adapter already handles local removal
    }
}