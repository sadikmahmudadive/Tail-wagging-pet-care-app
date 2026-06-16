package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicalRecordsFragment extends Fragment {

    private Pet selectedPet;
    private RecyclerView rvPastVaccinations, rvPastAllergies, rvPastCough, rvProHistory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoProHistory;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medical_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        if (getArguments() != null) {
            selectedPet = getArguments().getParcelable("SELECTED_PET");
        }

        rvPastVaccinations = view.findViewById(R.id.rvPastVaccinations);
        rvPastAllergies = view.findViewById(R.id.rvPastAllergies);
        rvPastCough = view.findViewById(R.id.rvPastCough);
        rvProHistory = view.findViewById(R.id.rvProfessionalHistory);
        tvNoProHistory = view.findViewById(R.id.tvNoProHistory);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshMedical);

        rvProHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        loadMedicalHistory();
        fetchProfessionalHistory();

        view.findViewById(R.id.btnSeeAllPastVaccinations).setOnClickListener(v -> openCalendarWithCategory("Vaccination"));
        view.findViewById(R.id.btnSeeAllTreatments).setOnClickListener(v -> openCalendarWithCategory("Medication"));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMedicalHistory();
            fetchProfessionalHistory();
        });
    }

    private void fetchProfessionalHistory() {
        if (selectedPet == null) return;

        dbRef.child("service_records").orderByChild("petId").equalTo(selectedPet.getPetID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ServiceRecord> proList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ServiceRecord record = ds.getValue(ServiceRecord.class);
                            if (record != null) proList.add(record);
                        }
                        Collections.reverse(proList);
                        rvProHistory.setAdapter(new ServiceRecordAdapter(proList));
                        tvNoProHistory.setVisibility(proList.isEmpty() ? View.VISIBLE : View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMedicalHistory();
        fetchProfessionalHistory();
    }

    private void loadMedicalHistory() {
        if (selectedPet == null) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        List<Event> allEvents = EventStore.getInstance(getContext()).getAllEvents();
        List<Event> pastVaccinations = new ArrayList<>();
        List<Event> pastAllergies = new ArrayList<>();
        List<Event> pastCough = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.petId != null && event.petId.equals(selectedPet.getPetID())) {
                if ("Vaccination".equalsIgnoreCase(event.category)) {
                    pastVaccinations.add(event);
                } else if ("Medication".equalsIgnoreCase(event.category)) {
                    if (event.title != null && event.title.toLowerCase().contains("cough")) {
                        pastCough.add(event);
                    } else {
                        pastAllergies.add(event);
                    }
                }
            }
        }

        rvPastVaccinations.setAdapter(new HealthVaccinationAdapter(pastVaccinations));
        rvPastAllergies.setAdapter(new HealthAllergyAdapter(pastAllergies));
        rvPastCough.setAdapter(new HealthAllergyAdapter(pastCough));

        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
    }

    private void openCalendarWithCategory(String category) {
        Intent intent = new Intent(getContext(), Calendar.class);
        if (selectedPet != null) {
            intent.putExtra("PRE_SELECTED_PET_ID", selectedPet.getPetID());
        }
        intent.putExtra("PRE_SELECTED_CATEGORY", category);
        startActivity(intent);
    }
}