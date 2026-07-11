package com.example.tailwagging;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminLogsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private ProgressBar pbLogs;
    private UserLogAdapter adapter;
    private final List<UserLog> logList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_logs);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("user_interaction_logs");

        rvLogs = findViewById(R.id.rvAdminLogs);
        pbLogs = findViewById(R.id.pbLogs);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserLogAdapter(logList);
        rvLogs.setAdapter(adapter);

        fetchLogs();
    }

    private void fetchLogs() {
        pbLogs.setVisibility(View.VISIBLE);
        dbRef.limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserLog log = ds.getValue(UserLog.class);
                    if (log != null) logList.add(log);
                }
                logList.sort((o1, o2) -> Long.compare(o2.timestamp, o1.timestamp)); // Newest first
                adapter.notifyDataSetChanged();
                pbLogs.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbLogs.setVisibility(View.GONE);
                Toast.makeText(AdminLogsActivity.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
