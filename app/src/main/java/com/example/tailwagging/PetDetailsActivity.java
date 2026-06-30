package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetDetailsActivity extends AppCompatActivity {

    private ImageView imageViewPetDetail, imageViewGenderDetail;
    private TextView textViewPetNameDetail, textViewPetBreedDetail, textViewPetAgeDetail,
            textViewPetColorDetail, textViewPetHeightDetail, textViewPetWeightDetail,
            textViewPetDescriptionDetail, textViewVaccinationDetail, textViewMedicationDetail,
            labelAbout, labelStatus, tvNoHistory;
    private CardView cardGender;
    private ImageButton buttonClosePetDetails, buttonEditPet;
    private Button buttonDeletePet;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvHistory;
    private ServiceRecordAdapter historyAdapter;
    private final List<ServiceRecord> historyList = new ArrayList<>();

    private Pet selectedPet;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.petDetailsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        // Initialize Views
        imageViewPetDetail = findViewById(R.id.imageViewPetDetail);
        imageViewGenderDetail = findViewById(R.id.imageViewGenderDetail);
        textViewPetNameDetail = findViewById(R.id.textViewPetNameDetail);
        textViewPetBreedDetail = findViewById(R.id.textViewPetBreedDetail);
        textViewPetAgeDetail = findViewById(R.id.textViewPetAgeDetail);
        textViewPetColorDetail = findViewById(R.id.textViewPetColorDetail);
        textViewPetHeightDetail = findViewById(R.id.textViewPetHeightDetail);
        textViewPetWeightDetail = findViewById(R.id.textViewPetWeightDetail);
        textViewPetDescriptionDetail = findViewById(R.id.textViewPetDescriptionDetail);
        textViewVaccinationDetail = findViewById(R.id.textViewVaccinationDetail);
        textViewMedicationDetail = findViewById(R.id.textViewMedicationDetail);
        labelAbout = findViewById(R.id.labelAbout);
        labelStatus = findViewById(R.id.labelStatus);
        cardGender = findViewById(R.id.cardGender);
        buttonClosePetDetails = findViewById(R.id.buttonClosePetDetails);
        buttonEditPet = findViewById(R.id.buttonEditPet);
        buttonDeletePet = findViewById(R.id.buttonDeletePet);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutPetDetails);
        
        rvHistory = findViewById(R.id.rvServiceHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);

        // Status Section Buttons
        findViewById(R.id.buttonContactVet).setOnClickListener(v -> launchPetHealthActivity());
        findViewById(R.id.buttonCheckFood).setOnClickListener(v -> launchPetHealthActivity());
        findViewById(R.id.buttonWhistle).setOnClickListener(v -> launchPetHealthActivity());

        // Get Pet data from Intent
        selectedPet = getIntent().getParcelableExtra("SELECTED_PET");

        if (selectedPet != null) {
            populatePetDetails(selectedPet);
            setupHistoryList();
            fetchServiceHistory();
        } else {
            Toast.makeText(this, "Pet details not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonClosePetDetails.setOnClickListener(v -> finish());
        buttonEditPet.setOnClickListener(v -> launchEditPetActivity());

        buttonDeletePet.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        swipeRefreshLayout.setOnRefreshListener(this::refreshPetData);
        NavbarHelper.setupNavbar(this);
    }

    private void setupHistoryList() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new ServiceRecordAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);
    }

    private void fetchServiceHistory() {
        dbRef.child("service_records").orderByChild("petId").equalTo(selectedPet.getPetID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ServiceRecord record = ds.getValue(ServiceRecord.class);
                            if (record != null) historyList.add(record);
                        }
                        Collections.reverse(historyList);
                        historyAdapter.notifyDataSetChanged();
                        tvNoHistory.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void launchEditPetActivity() {
        Intent intent = new Intent(PetDetailsActivity.this, AddEditPet.class);
        intent.putExtra("EDIT_PET", selectedPet);
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pet")
                .setMessage("Are you sure you want to delete this pet profile?")
                .setPositiveButton("Delete", (dialog, which) -> deletePet())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchPetHealthActivity() {
        Intent intent = new Intent(PetDetailsActivity.this, PetHealthActivity.class);
        intent.putExtra("SELECTED_PET", selectedPet);
        // Direct to tab index 2 (AI Scanner)
        intent.putExtra("START_TAB", 2);
        startActivity(intent);
    }

    private void deletePet() {
        if (selectedPet == null || selectedPet.getPetID() == null) {
            Toast.makeText(this, "Pet information is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Reference to pet in database
        DatabaseReference petRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/")
                .getReference("pets")
                .child(selectedPet.getPetID());

        petRef.removeValue()
                .addOnSuccessListener(unused -> {
                    // Remove associated birthday events
                    EventStore.getInstance(PetDetailsActivity.this).removeEventsByPetId(selectedPet.getPetID());

                    Toast.makeText(PetDetailsActivity.this, "Pet deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PetDetailsActivity.this, "Failed to delete pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedPet != null && selectedPet.getPetID() != null) {
            refreshPetData();
        }
    }

    private void refreshPetData() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        DatabaseReference petRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/")
                .getReference("pets")
                .child(selectedPet.getPetID());

        petRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    selectedPet = dataSnapshot.getValue(Pet.class);
                    if (selectedPet != null) {
                        populatePetDetails(selectedPet);
                    }
                } else {
                    // Pet might have been deleted from another screen
                    finish();
                }
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PetDetailsActivity.this, "Failed to refresh data", Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void populatePetDetails(Pet pet) {
        textViewPetNameDetail.setText(pet.getName());
        labelAbout.setText(getString(R.string.about_pet_placeholder, pet.getName()));
        labelStatus.setText(getString(R.string.status_pet_placeholder, pet.getName()));
        
        setTextOrDefault(textViewPetBreedDetail, pet.getBreed(), "N/A");
        
        // Calculate age from DOB if available
        String calculatedAge = calculateAgeFromDob(pet.getDob());
        if (!TextUtils.isEmpty(calculatedAge)) {
            textViewPetAgeDetail.setText(calculatedAge);
        } else {
            setTextOrDefault(textViewPetAgeDetail, pet.getAge(), "N/A");
        }
        
        setTextOrDefault(textViewPetColorDetail, pet.getColor(), "N/A");
        setTextOrDefault(textViewPetHeightDetail, pet.getHeight(), "N/A");
        setTextOrDefault(textViewPetWeightDetail, pet.getWeight(), "N/A");
        
        // Use dedicated description field
        setTextOrDefault(textViewPetDescriptionDetail, pet.getDescription(), "No description provided.");
        
        setTextOrDefault(textViewVaccinationDetail, "Last Vaccinated (2mon Ago)", "N/A");
        setTextOrDefault(textViewMedicationDetail, "Last Fed (1h Ago)", "N/A");

        if (pet.getGender() != null) {
            if (pet.getGender().equalsIgnoreCase("Female")) {
                imageViewGenderDetail.setImageResource(R.drawable.ic_female);
                cardGender.setCardBackgroundColor(ContextCompat.getColor(this, R.color.gender_female_bg));
            } else {
                imageViewGenderDetail.setImageResource(R.drawable.ic_male);
                cardGender.setCardBackgroundColor(ContextCompat.getColor(this, R.color.gender_male_bg));
            }
        }

        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(imageViewPetDetail);
        } else {
            imageViewPetDetail.setImageResource(R.drawable.ic_pet_placeholder);
        }
    }

    private String calculateAgeFromDob(String dob) {
        if (TextUtils.isEmpty(dob)) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(dob);
            if (birthDate == null) return null;

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar now = Calendar.getInstance();

            int years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            int months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH);

            // Adjust months if current day is before DOB day
            if (now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                months--;
            }

            if (months < 0) {
                years--;
                months += 12;
            }

            StringBuilder ageBuilder = new StringBuilder();
            if (years > 0) {
                ageBuilder.append(years).append(years == 1 ? " Year, " : " Years, ");
                ageBuilder.append(months).append(months == 1 ? " Month" : " Months");
            } else if (months >= 0) {
                ageBuilder.append(months).append(months == 1 ? " Month" : " Months");
            } else {
                return "Future Date";
            }

            return ageBuilder.toString().trim();

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setTextOrDefault(TextView textView, String text, String defaultValue) {
        if (textView != null) {
            if (!TextUtils.isEmpty(text)) {
                textView.setText(text);
            } else {
                textView.setText(defaultValue);
            }
        }
    }
}