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

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsFragment extends Fragment {

    private Pet selectedPet;
    private RecyclerView rvPastVaccinations, rvPastAllergies, rvPastCough;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medical_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedPet = getArguments().getParcelable("SELECTED_PET");
        }

        rvPastVaccinations = view.findViewById(R.id.rvPastVaccinations);
        rvPastAllergies = view.findViewById(R.id.rvPastAllergies);
        rvPastCough = view.findViewById(R.id.rvPastCough);

        loadMedicalHistory();

        view.findViewById(R.id.btnSeeAllPastVaccinations).setOnClickListener(v -> openCalendarWithCategory("Vaccination"));
        view.findViewById(R.id.btnSeeAllTreatments).setOnClickListener(v -> openCalendarWithCategory("Medication"));
    }

    private void loadMedicalHistory() {
        if (selectedPet == null) return;

        List<Event> allEvents = EventStore.getInstance(getContext()).getAllEvents();
        List<Event> pastVaccinations = new ArrayList<>();
        List<Event> pastAllergies = new ArrayList<>();
        List<Event> pastCough = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.petId != null && event.petId.equals(selectedPet.getPetID())) {
                if ("Vaccination".equalsIgnoreCase(event.category)) {
                    pastVaccinations.add(event);
                } else if ("Medication".equalsIgnoreCase(event.category)) {
                    // Logic to separate Allergies and Cough if possible, otherwise group them
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