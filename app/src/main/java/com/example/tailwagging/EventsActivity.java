package com.example.tailwagging;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

public class EventsActivity extends AppCompatActivity implements TodayEventAdapter.OnEventChangedListener {
    private RecyclerView recyclerView;
    private TodayEventAdapter adapter;
    private ImageButton btnBack;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rvAllEvents);
        btnBack = findViewById(R.id.btnBack);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutEvents);
        
        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadEvents();

        swipeRefreshLayout.setOnRefreshListener(this::loadEvents);
        NavbarHelper.setupNavbar(this);
    }

    private void loadEvents() {
        List<Event> allEvents = EventStore.getInstance(this).getAllEvents();
        // Sort by date, then time
        allEvents.sort((e1, e2) -> {
            int dateComp = e1.date.compareTo(e2.date);
            if (dateComp != 0) return dateComp;
            return e1.fromTime.compareTo(e2.fromTime);
        });

        adapter = new TodayEventAdapter(this, allEvents, this);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    @Override
    public void onEventDeleted() {
        loadEvents();
    }
}