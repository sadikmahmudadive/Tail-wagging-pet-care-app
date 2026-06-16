package com.example.tailwagging;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientListActivity extends AppCompatActivity {

    private RecyclerView rvClients;
    private TextView tvNoClients;
    private DatabaseReference dbRef;
    private String providerId;
    private final List<Appointment> clientAppointments = new ArrayList<>();
    private VetAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clientListRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        providerId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        rvClients = findViewById(R.id.rvClients);
        tvNoClients = findViewById(R.id.tvNoClients);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvClients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VetAppointmentAdapter(this, clientAppointments);
        rvClients.setAdapter(adapter);

        fetchClients();
        NavbarHelper.setupNavbar(this);
    }

    private void fetchClients() {
        if (providerId == null) return;

        dbRef.child("appointments").orderByChild("vetId").equalTo(providerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        clientAppointments.clear();
                        Set<String> uniquePets = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Appointment appt = ds.getValue(Appointment.class);
                            if (appt != null && appt.petId != null && !uniquePets.contains(appt.petId)) {
                                uniquePets.add(appt.petId);
                                clientAppointments.add(appt);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvNoClients.setVisibility(clientAppointments.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ClientListActivity.this, "Failed to load clients", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}