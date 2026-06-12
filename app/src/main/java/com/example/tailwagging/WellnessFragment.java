package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class WellnessFragment extends Fragment {

    private Pet selectedPet;
    private RecyclerView rvVaccinations, rvAllergies;
    private HealthVaccinationAdapter vaccinationAdapter;
    private HealthAllergyAdapter allergyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wellness, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            selectedPet = getArguments().getParcelable("SELECTED_PET");
        }

        rvVaccinations = view.findViewById(R.id.rvVaccinations);
        rvAllergies = view.findViewById(R.id.rvAllergies);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshWellness);

        loadPetHealthData();

        view.findViewById(R.id.btnSeeAllVaccinations).setOnClickListener(v -> openCalendarWithCategory("Vaccination"));
        view.findViewById(R.id.btnSeeAllAllergies).setOnClickListener(v -> openCalendarWithCategory("Medication"));
        view.findViewById(R.id.btnStartAppointment).setOnClickListener(v -> openCalendarWithCategory("Vet Appointment"));

        swipeRefreshLayout.setOnRefreshListener(this::loadPetHealthData);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPetHealthData();
    }

    private void loadPetHealthData() {
        if (selectedPet == null) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        List<Event> allEvents = EventStore.getInstance(getContext()).getAllEvents();
        List<Event> petVaccinations = new ArrayList<>();
        List<Event> petAllergies = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.petId != null && event.petId.equals(selectedPet.getPetID())) {
                if ("Vaccination".equalsIgnoreCase(event.category)) {
                    petVaccinations.add(event);
                } else if ("Medication".equalsIgnoreCase(event.category)) {
                    petAllergies.add(event);
                }
            }
        }

        vaccinationAdapter = new HealthVaccinationAdapter(petVaccinations);
        rvVaccinations.setAdapter(vaccinationAdapter);

        allergyAdapter = new HealthAllergyAdapter(petAllergies);
        rvAllergies.setAdapter(allergyAdapter);

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