package com.example.tailwagging;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetServicesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 102;
    private enum ServiceType { VETERINARY, GROOMING, BOARDING }
    private ServiceType currentType = ServiceType.VETERINARY;

    private LinearLayout catVeterinary, catGrooming, catBoarding;
    private MaterialCardView cardVetIcon, cardGroomIcon, cardBoardingIcon;
    private View btnUploadDiseasePhoto, layoutFindDisease;
    private TextView tvNearbyTitle, tvRecommendedTitle;
    private RecyclerView rvNearby, rvRecommended;
    private EditText etSearch;
    private DatabaseReference dbRef;
    private Double userLat, userLng;
    private List<Vet> allLoadedServices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_services);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.servicesRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        fetchUserLocation();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        catVeterinary = findViewById(R.id.catVeterinary);
        catGrooming = findViewById(R.id.catGrooming);
        catBoarding = findViewById(R.id.catBoarding);

        cardVetIcon = findViewById(R.id.cardVetIcon);
        cardGroomIcon = findViewById(R.id.cardGroomIcon);
        cardBoardingIcon = findViewById(R.id.cardBoardingIcon);

        layoutFindDisease = findViewById(R.id.layoutFindDisease);
        btnUploadDiseasePhoto = findViewById(R.id.uploadPhotoFrame);
        tvNearbyTitle = findViewById(R.id.tvNearbyTitle);
        tvRecommendedTitle = findViewById(R.id.tvRecommendedTitle);

        rvNearby = findViewById(R.id.rvNearbyServices);
        rvRecommended = findViewById(R.id.rvRecommendedServices);

        rvNearby.setLayoutManager(new LinearLayoutManager(this));
        rvRecommended.setLayoutManager(new LinearLayoutManager(this));

        etSearch = findViewById(R.id.etSearchServices);
        setupSearch();

        catVeterinary.setOnClickListener(v -> switchService(ServiceType.VETERINARY));
        catGrooming.setOnClickListener(v -> switchService(ServiceType.GROOMING));
        catBoarding.setOnClickListener(v -> switchService(ServiceType.BOARDING));

        btnUploadDiseasePhoto.setOnClickListener(v -> openImagePicker());

        NavbarHelper.setupNavbar(this);
        
        // Initial state
        switchService(ServiceType.VETERINARY);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterServices(String query) {
        if (query.isEmpty()) {
            ServiceAdapter adapter = new ServiceAdapter(this, allLoadedServices);
            rvNearby.setAdapter(adapter);
            rvRecommended.setAdapter(adapter);
            return;
        }

        List<Vet> filteredList = new ArrayList<>();
        for (Vet service : allLoadedServices) {
            if ((service.getName() != null && service.getName().toLowerCase().contains(query.toLowerCase())) ||
                (service.getQualification() != null && service.getQualification().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(service);
            }
        }

        ServiceAdapter adapter = new ServiceAdapter(this, filteredList);
        rvNearby.setAdapter(adapter);
        rvRecommended.setAdapter(adapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri == null) return;

            Log.d("PetServicesActivity", "Image selected for AI Health Scanner: " + imageUri.toString());
            Toast.makeText(this, "Uploading to Gemini AI...", Toast.LENGTH_SHORT).show();

            // Show loading state
            ProgressBar pb = findViewById(R.id.progressBarScanner);
            if (pb != null) pb.setVisibility(View.VISIBLE);
            
            String prompt = "You are a professional veterinary assistant. Analyze this photo of a pet's skin/body. " +
                    "Identify potential health issues (e.g. Dermatitis, Fleas, Fungal Infection). " +
                    "Provide your response in this exact format: " +
                    "DIAGNOSIS: [Name of potential issue] | SUGGESTION: [Short advice for the owner]";

            GeminiAiHelper.analyzePetImage(this, imageUri, prompt, new GeminiAiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String analysis) {
                    runOnUiThread(() -> {
                        if (pb != null) pb.setVisibility(View.GONE);
                        View resultContainer = findViewById(R.id.layoutDiseaseResult);
                        if (resultContainer != null) resultContainer.setVisibility(View.VISIBLE);

                        TextView tvDisease = findViewById(R.id.tvDiseaseName);
                        TextView tvDesc = findViewById(R.id.tvDiseaseDesc);

                        // Expected format: DIAGNOSIS: [Name] | SUGGESTION: [Advice]
                        if (analysis.contains("|")) {
                            String[] parts = analysis.split("\\|");
                            if (tvDisease != null) tvDisease.setText(parts[0].replace("DIAGNOSIS:", "").trim());
                            if (tvDesc != null) tvDesc.setText(parts[1].replace("SUGGESTION:", "").trim());
                        } else {
                            if (tvDisease != null) tvDisease.setText("AI Analysis Result");
                            if (tvDesc != null) tvDesc.setText(analysis);
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        if (pb != null) pb.setVisibility(View.GONE);
                        Toast.makeText(PetServicesActivity.this, "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private void switchService(ServiceType type) {
        currentType = type;
        
        int activeBg = getColor(R.color.md_theme_light_primaryContainer);
        int activeTint = getColor(R.color.md_theme_light_primary);
        int inactiveBg = Color.WHITE;
        int inactiveTint = getColor(R.color.grey_dark);

        // Reset backgrounds
        cardVetIcon.setCardBackgroundColor(inactiveBg);
        cardGroomIcon.setCardBackgroundColor(inactiveBg);
        cardBoardingIcon.setCardBackgroundColor(inactiveBg);
        
        // Reset tints
        // Actually, the ImageView is the only child of the card or inside a layout? 
        // In the new XML: MaterialCardView -> ImageView (id is not set on the ImageView itself, but it is the first child)
        
        // Let's use getChildAt(0) safely
        if (cardVetIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardVetIcon.getChildAt(0)).setColorFilter(inactiveTint);
        if (cardGroomIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardGroomIcon.getChildAt(0)).setColorFilter(inactiveTint);
        if (cardBoardingIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardBoardingIcon.getChildAt(0)).setColorFilter(inactiveTint);

        switch (type) {
            case VETERINARY:
                cardVetIcon.setCardBackgroundColor(activeBg);
                if (cardVetIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardVetIcon.getChildAt(0)).setColorFilter(activeTint);
                if (layoutFindDisease != null) layoutFindDisease.setVisibility(View.VISIBLE);
                tvNearbyTitle.setText("Nearby Veterinarians");
                tvRecommendedTitle.setText("Recommended Experts");
                fetchServices("Veterinarian");
                break;
            case GROOMING:
                cardGroomIcon.setCardBackgroundColor(activeBg);
                if (cardGroomIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardGroomIcon.getChildAt(0)).setColorFilter(activeTint);
                if (layoutFindDisease != null) layoutFindDisease.setVisibility(View.GONE);
                tvNearbyTitle.setText("Nearby Grooming");
                tvRecommendedTitle.setText("Top Groomers");
                fetchServices("Grooming");
                break;
            case BOARDING:
                cardBoardingIcon.setCardBackgroundColor(activeBg);
                if (cardBoardingIcon.getChildAt(0) instanceof ImageView) ((ImageView)cardBoardingIcon.getChildAt(0)).setColorFilter(activeTint);
                if (layoutFindDisease != null) layoutFindDisease.setVisibility(View.GONE);
                tvNearbyTitle.setText("Nearby Boarding");
                tvRecommendedTitle.setText("Trusted Stays");
                fetchServices("Boarding");
                break;
        }
    }

    private void fetchUserLocation() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLat = snapshot.child("latitude").getValue(Double.class);
                userLng = snapshot.child("longitude").getValue(Double.class);

                String role = snapshot.child("role").getValue(String.class);
                if (role != null) {
                    String cachedRole = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_role", "Pet Owner");
                    if (!role.trim().equalsIgnoreCase(cachedRole.trim())) {
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role.trim()).apply();
                        NavbarHelper.setupNavbar(PetServicesActivity.this);
                    }
                }

                // Refresh list if we already switched
                fetchServices("Veterinarian"); 
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchServices(String role) {
        dbRef.child("users").orderByChild("role").equalTo(role)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Vet> list = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String qualification = snapshot.child("qualification").getValue(String.class);
                            String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String id = snapshot.getKey();
                            
                            Double vLat = snapshot.child("latitude").getValue(Double.class);
                            Double vLng = snapshot.child("longitude").getValue(Double.class);
                            
                            String distance = "Local";
                            if (userLat != null && userLng != null && vLat != null && vLng != null) {
                                float[] results = new float[1];
                                Location.distanceBetween(userLat, userLng, vLat, vLng, results);
                                distance = String.format(Locale.getDefault(), "%.1f km", results[0] / 1000);
                            }

                            float rating = 4.5f;
                            Object rVal = snapshot.child("rating").getValue();
                            if (rVal != null) rating = ((Number) rVal).floatValue();

                            int reviews = 0;
                            Integer cVal = snapshot.child("reviewsCount").getValue(Integer.class);
                            if (cVal != null) reviews = cVal;

                            Vet vet = new Vet(id, name, qualification, rating, reviews, "Expert", distance, "N/A", "10 years", "N/A", R.drawable.ic_profile);
                            if (photoUrl != null && !photoUrl.isEmpty()) vet.setImageUrl(photoUrl);
                            if (phone != null) vet.setPhone(phone);
                            list.add(vet);
                        }
                        
                        // Mock data for display if list is empty
                        if (list.isEmpty()) {
                            if ("Veterinarian".equals(role)) {
                                list.add(new Vet("v1", "Dr. Nambuvan", "Bachelor of veterinary science", 5.0f, 100, "Expert", "2.5 km", "1000 LKR", "10 years", "25/11/2022", R.drawable.pet1));
                                list.add(new Vet("v2", "Dr. Sambuvan", "Veterinary Dentist", 5.0f, 80, "Senior", "2.0 km", "1200 LKR", "8 years", "N/A", R.drawable.ic_profile));
                            } else if ("Grooming".equals(role)) {
                                list.add(new Vet("g1", "Comb and Collar", "Expert Grooming", 5.0f, 150, "Top Rated", "2.5 km", "1500 LKR", "5 years", "N/A", R.drawable.pet1));
                                list.add(new Vet("g2", "Cosmo Dog Cares", "Full Service Spa", 4.8f, 90, "Luxury", "3.1 km", "2000 LKR", "3 years", "N/A", R.drawable.ic_profile));
                            } else {
                                list.add(new Vet("b1", "Tails of the city", "Dog Boarding", 5.0f, 200, "Spacious", "4.0 km", "2500 LKR", "7 years", "N/A", R.drawable.pet1));
                                list.add(new Vet("b2", "Cutie Paws", "Home Environment", 4.9f, 60, "Cozy", "1.5 km", "1800 LKR", "4 years", "N/A", R.drawable.ic_profile));
                            }
                        }

                        allLoadedServices.clear();
                        allLoadedServices.addAll(list);

                        // Reset search box when switching categories
                        etSearch.setText("");

                        ServiceAdapter adapter = new ServiceAdapter(PetServicesActivity.this, list);
                        rvNearby.setAdapter(adapter);
                        rvRecommended.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }
}