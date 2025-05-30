package com.example.tailwagging;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class PetDetailsActivity extends AppCompatActivity {

    private CircleImageView imageViewPetDetail;
    private TextView textViewPetNameDetail, textViewPetBreedDetail, textViewPetAgeDetail,
            textViewPetGenderDetail, textViewPetDobDetail, textViewPetColorDetail,
            textViewPetHeightDetail, textViewPetWeightDetail, textViewPetSoundDetail,
            textViewVaccinationDetail, textViewMedicationDetail;
    private ImageButton buttonClosePetDetails;
    private Button buttonDeletePet;

    private Pet selectedPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);

        // --- Apply Dialog/Floating Window Style ---
        WindowManager.LayoutParams params = getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        params.width = (int) (screenWidth * 0.90);
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.6f;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Initialize Views
        imageViewPetDetail = findViewById(R.id.imageViewPetDetail);
        textViewPetNameDetail = findViewById(R.id.textViewPetNameDetail);
        textViewPetBreedDetail = findViewById(R.id.textViewPetBreedDetail);
        textViewPetAgeDetail = findViewById(R.id.textViewPetAgeDetail);
        textViewPetGenderDetail = findViewById(R.id.textViewPetGenderDetail);
        textViewPetDobDetail = findViewById(R.id.textViewPetDobDetail);
        textViewPetColorDetail = findViewById(R.id.textViewPetColorDetail);
        textViewPetHeightDetail = findViewById(R.id.textViewPetHeightDetail);
        textViewPetWeightDetail = findViewById(R.id.textViewPetWeightDetail);
        textViewPetSoundDetail = findViewById(R.id.textViewPetSoundDetail);
        textViewVaccinationDetail = findViewById(R.id.textViewVaccinationDetail);
        textViewMedicationDetail = findViewById(R.id.textViewMedicationDetail);
        buttonClosePetDetails = findViewById(R.id.buttonClosePetDetails);
        buttonDeletePet = findViewById(R.id.buttonDeletePet);

        // Get Pet data from Intent
        selectedPet = getIntent().getParcelableExtra("SELECTED_PET");

        if (selectedPet != null) {
            populatePetDetails(selectedPet);
        } else {
            Toast.makeText(this, "Pet details not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonClosePetDetails.setOnClickListener(v -> finish());

        buttonDeletePet.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pet")
                .setMessage("Are you sure you want to delete this pet profile?")
                .setPositiveButton("Delete", (dialog, which) -> deletePet())
                .setNegativeButton("Cancel", null)
                .show();
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
                    Toast.makeText(PetDetailsActivity.this, "Pet deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PetDetailsActivity.this, "Failed to delete pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populatePetDetails(Pet pet) {
        textViewPetNameDetail.setText(pet.getName());
        setTextOrDefault(textViewPetBreedDetail, pet.getBreed(), "N/A");
        setTextOrDefault(textViewPetAgeDetail, pet.getAge(), "N/A");
        setTextOrDefault(textViewPetGenderDetail, pet.getGender(), "N/A");
        setTextOrDefault(textViewPetDobDetail, pet.getDob(), "N/A");
        setTextOrDefault(textViewPetColorDetail, pet.getColor(), "N/A");
        setTextOrDefault(textViewPetHeightDetail, pet.getHeight(), "N/A");
        setTextOrDefault(textViewPetWeightDetail, pet.getWeight(), "N/A");
        setTextOrDefault(textViewPetSoundDetail, pet.getSound(), "N/A");
        setTextOrDefault(textViewVaccinationDetail, pet.getVaccinationDetails(), "No vaccination details recorded.");
        setTextOrDefault(textViewMedicationDetail, pet.getMedicationTime(), "No medication schedule recorded.");

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