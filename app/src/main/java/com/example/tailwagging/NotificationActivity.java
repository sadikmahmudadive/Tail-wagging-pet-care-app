package com.example.tailwagging;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutNoNotifications;
    private DatabaseReference dbRef;
    private String userId;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        rvNotifications = findViewById(R.id.rvNotifications);
        swipeRefresh = findViewById(R.id.swipeRefreshNotifications);
        layoutNoNotifications = findViewById(R.id.layoutNoNotifications);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnClearAll = findViewById(R.id.btnClearAll);

        btnBack.setOnClickListener(v -> finish());
        btnClearAll.setOnClickListener(v -> clearAllNotifications());

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchNotifications);

        fetchNotifications();
        NavbarHelper.setupNavbar(this);
    }

    private void fetchNotifications() {
        if (userId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Viewing offline notifications", Toast.LENGTH_SHORT).show();
        }

        dbRef.child("notifications").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationItem item = ds.getValue(NotificationItem.class);
                    if (item != null) {
                        notificationList.add(item);
                    }
                }
                Collections.reverse(notificationList); // Show newest first
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
                layoutNoNotifications.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                
                // Mark all as read when viewed
                markAllAsRead();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void markAllAsRead() {
        if (userId == null || notificationList.isEmpty()) return;
        dbRef.child("notifications").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().child("isRead").setValue(true);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clearAllNotifications() {
        if (userId == null) return;
        dbRef.child("notifications").child(userId).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cleared all notifications", Toast.LENGTH_SHORT).show();
        });
    }
}