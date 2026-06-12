package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VetDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeVet, tvVetGreeting, tvTodayApptsCount, tvTotalPatients;
    private CircleImageView ivVetProfile;
    private Button btnLogout;
    private RecyclerView rvAppointments;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference dbRef;
    private String vetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_dashboard);

        tvWelcomeVet = findViewById(R.id.tvWelcomeVet);
        tvVetGreeting = findViewById(R.id.tvVetGreeting);
        tvTodayApptsCount = findViewById(R.id.tvTodayApptsCount);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        ivVetProfile = findViewById(R.id.vetProfilePhoto);
        btnLogout = findViewById(R.id.btnLogoutVet);
        rvAppointments = findViewById(R.id.rvVetAppointments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutVet);

        vetId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        setDynamicGreeting();
        setupNavigationBar();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchVetData();
            fetchAppointments();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        });

        fetchVetData();
        fetchAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchVetData();
        fetchAppointments();
    }

    private void setupNavigationBar() {
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, Calendar.class);
                startActivity(intent);
            });
        }

        View navAddPet = findViewById(R.id.navAddPet);
        if (navAddPet != null) {
            navAddPet.setOnClickListener(v -> startActivity(new Intent(this, AddEditPet.class)));
        }

        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        }
    }

    private void setDynamicGreeting() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int timeOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 5 && timeOfDay < 12) {
            tvVetGreeting.setText(R.string.good_morning);
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            tvVetGreeting.setText(R.string.good_afternoon);
        } else if (timeOfDay >= 17 && timeOfDay < 21) {
            tvVetGreeting.setText(R.string.good_evening);
        } else {
            tvVetGreeting.setText(R.string.good_night);
        }
    }

    private void fetchVetData() {
        if (vetId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        dbRef.child("users").child(vetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                
                tvWelcomeVet.setText(name != null ? "Dr. " + name : "Dr. Expert");
                
                Glide.with(VetDashboardActivity.this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(ivVetProfile);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchAppointments() {
        if (vetId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        dbRef.child("appointments").orderByChild("vetId").equalTo(vetId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<MapAppointment> list = new ArrayList<>();
                        int todayCount = 0;
                        String todayStr = LocalDate.now().toString();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MapAppointment appt = snapshot.getValue(MapAppointment.class);
                            if (appt != null) {
                                list.add(appt);
                                if (todayStr.equals(appt.date)) {
                                    todayCount++;
                                }
                            }
                        }
                        
                        tvTodayApptsCount.setText(String.valueOf(todayCount));
                        tvTotalPatients.setText(String.valueOf(list.size()));
                        
                        VetAppointmentAdapter adapter = new VetAppointmentAdapter(VetDashboardActivity.this, list);
                        rvAppointments.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
    
    public static class MapAppointment {
        public String id, userId, ownerName, petName, petImageUrl, date, time, status, vetId, vetName, petId;
        public MapAppointment() {}
    }
}