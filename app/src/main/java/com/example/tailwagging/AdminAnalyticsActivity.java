package com.example.tailwagging;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private TextView tvUsers, tvPets, tvVets, tvAppointments;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_analytics);

        tvUsers = findViewById(R.id.tvCountUsers);
        tvPets = findViewById(R.id.tvCountPets);
        tvVets = findViewById(R.id.tvCountVets);
        tvAppointments = findViewById(R.id.tvCountAppointments);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        loadAnalytics();
    }

    private void loadAnalytics() {
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

        // Appointments
        dbRef.child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvAppointments.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
