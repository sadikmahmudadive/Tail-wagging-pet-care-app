package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class AdminDashboardActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private TextView tvUsers, tvPets, tvVets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        tvUsers = findViewById(R.id.tvAdminStatUsers);
        tvPets = findViewById(R.id.tvAdminStatPets);
        tvVets = findViewById(R.id.tvAdminStatVets);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadDashboardStats();

        findViewById(R.id.btnManageUsers).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminUserListActivity.class)));
            
        findViewById(R.id.btnManageVets).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminVerifyVetsActivity.class)));
            
        findViewById(R.id.btnViewAnalytics).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminAnalyticsActivity.class)));

        findViewById(R.id.btnPushBroadcast).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminBroadcastActivity.class)));

        findViewById(R.id.btnUserView).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        findViewById(R.id.btnLogoutAdmin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    private void loadDashboardStats() {
        // Users & Vets
        dbRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = snapshot.getChildrenCount();
                long totalVets = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String role = ds.child("role").getValue(String.class);
                    if ("Veterinarian".equalsIgnoreCase(role)) totalVets++;
                }
                tvUsers.setText(String.valueOf(totalUsers));
                tvVets.setText(String.valueOf(totalVets));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Pets
        dbRef.child("pets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvPets.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
