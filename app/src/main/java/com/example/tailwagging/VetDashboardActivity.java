package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class VetDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeVet, tvVetGreeting, tvTodayApptsCount, tvTotalPatients, tvVetRatingScore;
    private TextView tvTodayLabel, tvPatientsLabel;
    private CircleImageView ivVetProfile;
    private ImageButton btnLogout;
    private RecyclerView rvAppointments;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference dbRef;
    private String vetId;
    private final List<Appointment> appointmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vet_dashboard);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.vetRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeVet = findViewById(R.id.tvWelcomeVet);
        tvVetGreeting = findViewById(R.id.tvVetGreeting);
        tvTodayApptsCount = findViewById(R.id.tvTodayApptsCount);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvVetRatingScore = findViewById(R.id.tvVetRatingScore);
        
        tvTodayLabel = findViewById(R.id.tvTodayLabel);
        tvPatientsLabel = findViewById(R.id.tvPatientsLabel);

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
        
        NavbarHelper.setupNavbar(this);
        // Handle the unique "Add Action" for providers
        View navAdd = findViewById(R.id.navProviderAdd);
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> showAddRecordDialog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchVetData();
        fetchAppointments();
    }

    private void setupNavigationBar() {
        // Special case for Home tab: refresh data instead of just activity switch
        View navHome = findViewById(R.id.navProviderHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                fetchVetData();
                fetchAppointments();
                Toast.makeText(this, "Refreshing Dashboard...", Toast.LENGTH_SHORT).show();
            });
        }

        // Special case for Provider Action button (Dialog)
        View navAdd = findViewById(R.id.navProviderAdd);
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> showAddRecordDialog());
        }

        // Other items are handled by NavbarHelper.setupNavbar(this) called in onCreate
    }

    private void showAddRecordDialog() {
        if (appointmentsList.isEmpty()) {
            Toast.makeText(this, "No active patients to add records for.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_service_record, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        Spinner spinner = view.findViewById(R.id.spinnerPetRecord);
        com.google.android.material.textfield.TextInputLayout layoutTitle = view.findViewById(R.id.layoutRecordTitle);
        EditText etTitle = view.findViewById(R.id.etRecordTitle);
        EditText etDesc = view.findViewById(R.id.etRecordDescription);
        Button btnSave = view.findViewById(R.id.btnSaveRecord);

        // Adjust title and hint based on role
        dbRef.child("users").child(vetId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userRole = snapshot.getValue(String.class);
                if ("Grooming".equalsIgnoreCase(userRole)) {
                    tvTitle.setText("Add Grooming Record");
                    layoutTitle.setHint("Service (e.g. Bath & Trim)");
                } else if ("Boarding".equalsIgnoreCase(userRole)) {
                    tvTitle.setText("Add Boarding Record");
                    layoutTitle.setHint("Stay Type (e.g. Overnight)");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        List<String> petNames = new ArrayList<>();
        for (Appointment a : appointmentsList) {
            petNames.add(a.petName + " (" + a.ownerName + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            int pos = spinner.getSelectedItemPosition();
            Appointment selectedAppt = appointmentsList.get(pos);
            
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Required");
                return;
            }

            String recordId = dbRef.child("service_records").push().getKey();
            ServiceRecord record = new ServiceRecord(
                    recordId,
                    selectedAppt.petId,
                    selectedAppt.petName,
                    vetId,
                    tvWelcomeVet.getText().toString(),
                    "Professional",
                    LocalDate.now().toString(),
                    title,
                    desc,
                    System.currentTimeMillis()
            );

            if (recordId != null) {
                dbRef.child("service_records").child(recordId).setValue(record)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            }
        });

        dialog.show();
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
                String role = snapshot.child("role").getValue(String.class);

                float rating = 0;
                Object rVal = snapshot.child("rating").getValue();
                if (rVal != null) rating = ((Number) rVal).floatValue();

                tvVetRatingScore.setText(String.format(Locale.getDefault(), "%.1f", rating));
                
                String displayTitle = "Expert";
                if ("Veterinarian".equalsIgnoreCase(role)) {
                    displayTitle = "Dr. " + (name != null ? name : "");
                    tvTodayLabel.setText("Today");
                    tvPatientsLabel.setText("Patients");
                } else if ("Grooming".equalsIgnoreCase(role)) {
                    displayTitle = "Groomer " + (name != null ? name : "");
                    tvTodayLabel.setText("Grooms");
                    tvPatientsLabel.setText("Clients");
                } else if ("Boarding".equalsIgnoreCase(role)) {
                    displayTitle = "Boarding " + (name != null ? name : "");
                    tvTodayLabel.setText("Stays");
                    tvPatientsLabel.setText("Pets");
                } else {
                    displayTitle = name != null ? name : "Expert";
                }

                tvWelcomeVet.setText(displayTitle);
                
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
                        appointmentsList.clear();
                        int todayCount = 0;
                        String todayStr = LocalDate.now().toString();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Appointment appt = snapshot.getValue(Appointment.class);
                            if (appt != null) {
                                appointmentsList.add(appt);
                                if (todayStr.equals(appt.date)) {
                                    todayCount++;
                                }
                            }
                        }
                        
                        tvTodayApptsCount.setText(String.valueOf(todayCount));
                        tvTotalPatients.setText(String.valueOf(appointmentsList.size()));
                        
                        VetAppointmentAdapter adapter = new VetAppointmentAdapter(VetDashboardActivity.this, appointmentsList);
                        rvAppointments.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}