package com.example.tailwagging;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PetFoodActivity extends AppCompatActivity {

    private EditText etCurrentFood;
    private AutoCompleteTextView etFoodType;
    private ChipGroup cgFeedingTimes;
    private TextView tvAiRecommendation;
    private List<String> feedingTimesList = new ArrayList<>();
    
    private Pet selectedPet;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_food);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedPet = getIntent().getParcelableExtra("SELECTED_PET");
        if (selectedPet == null) {
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("pets");

        initWidgets();
        populateData();
    }

    private void initWidgets() {
        etCurrentFood = findViewById(R.id.etCurrentFood);
        etFoodType = findViewById(R.id.etFoodType);
        cgFeedingTimes = findViewById(R.id.cgFeedingTimesFood);
        tvAiRecommendation = findViewById(R.id.tvAiRecommendation);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] types = {"Dry", "Wet", "Mixed", "Raw", "Homemade"};
        etFoodType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));
        etFoodType.setOnClickListener(v -> etFoodType.showDropDown());

        findViewById(R.id.btnSaveFood).setOnClickListener(v -> saveFoodData());
        findViewById(R.id.btnAddTimeFood).setOnClickListener(v -> showTimePickerDialog());
        findViewById(R.id.btnAiSuggestFood).setOnClickListener(v -> suggestAiSchedule());
        findViewById(R.id.btnGenerateAiGuide).setOnClickListener(v -> generateAiExpertGuide());
    }

    private void populateData() {
        etCurrentFood.setText(selectedPet.getCurrentFoodName());
        etFoodType.setText(selectedPet.getFoodType(), false);
        
        if (selectedPet.getFeedingTimes() != null) {
            feedingTimesList.clear();
            cgFeedingTimes.removeAllViews();
            for (String time : selectedPet.getFeedingTimes()) {
                feedingTimesList.add(time);
                addTimeChip(time);
            }
        }
    }

    private void addTimeChip(String time) {
        Chip chip = new Chip(this);
        chip.setText(time);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.health_tab_inactive);
        chip.setOnCloseIconClickListener(v -> {
            feedingTimesList.remove(time);
            cgFeedingTimes.removeView(chip);
        });
        cgFeedingTimes.addView(chip);
    }

    private void showTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (!feedingTimesList.contains(time)) {
                feedingTimesList.add(time);
                addTimeChip(time);
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveFoodData() {
        String foodName = etCurrentFood.getText().toString().trim();
        String foodType = etFoodType.getText().toString().trim();

        selectedPet.setCurrentFoodName(foodName);
        selectedPet.setFoodType(foodType);
        selectedPet.setFeedingTimes(feedingTimesList);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving...");
        pd.show();

        dbRef.child(selectedPet.getPetID()).setValue(selectedPet)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(this, "Diet plan updated!", Toast.LENGTH_SHORT).show();
                    // Reschedule alarms
                    AlarmHelper.setFeedingAlarms(this, selectedPet);
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void suggestAiSchedule() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Consulting AI...");
        pd.show();

        String prompt = "Suggest a daily feeding schedule (times in HH:MM, 24h, comma separated) for a " + 
                        selectedPet.getBreed() + ", aged " + selectedPet.getAge() + 
                        ", weight " + selectedPet.getWeight() + "kg. Return ONLY the times.";

        ChatGptAiHelper.generateText(prompt, new ChatGptAiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    parseAndAddTimes(result);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(PetFoodActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseAndAddTimes(String result) {
        String[] parts = result.split("[,\\s\\n]+");
        for (String p : parts) {
            String clean = p.replaceAll("[^0-9:]", "");
            if (clean.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                if (!feedingTimesList.contains(clean)) {
                    feedingTimesList.add(clean);
                    addTimeChip(clean);
                }
            }
        }
    }

    private void generateAiExpertGuide() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Generating nutritional guide...");
        pd.show();

        String prompt = "Provide a professional nutritional recommendation for a " + 
                        selectedPet.getBreed() + ", aged " + selectedPet.getAge() + 
                        ", weighing " + selectedPet.getWeight() + "kg. " +
                        "Suggest ingredients to look for or avoid. " +
                        "IMPORTANT: Return the response in clean, professional plain text. " +
                        "DO NOT use markdown formatting like ###, ** or others. Use simple dashes (-) for lists.";

        ChatGptAiHelper.generateText(prompt, new ChatGptAiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String analysis) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    // Basic cleanup just in case AI still adds markdown
                    String cleanText = analysis.replaceAll("[#*]", "").trim();
                    tvAiRecommendation.setText(cleanText);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(PetFoodActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
