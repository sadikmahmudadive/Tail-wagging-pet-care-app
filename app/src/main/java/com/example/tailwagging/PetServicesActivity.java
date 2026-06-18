package com.example.tailwagging;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout layoutFindDisease;
    private View btnUploadDiseasePhoto;
    private TextView tvNearbyTitle, tvRecommendedTitle;
    private RecyclerView rvNearby, rvRecommended;
    private DatabaseReference dbRef;
    private Double userLat, userLng;

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

        catVeterinary.setOnClickListener(v -> switchService(ServiceType.VETERINARY));
        catGrooming.setOnClickListener(v -> switchService(ServiceType.GROOMING));
        catBoarding.setOnClickListener(v -> switchService(ServiceType.BOARDING));

        btnUploadDiseasePhoto.setOnClickListener(v -> openImagePicker());

        NavbarHelper.setupNavbar(this);
        
        // Initial state
        switchService(ServiceType.VETERINARY);
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
            // Automatically launch prediction or similar logic
            Toast.makeText(this, "Photo uploaded. Identifying disease...", Toast.LENGTH_SHORT).show();
            // Mock result
            TextView tvDisease = findViewById(R.id.tvDiseaseName);
            if (tvDisease != null) {
                tvDisease.setText("Dermatitis (Mock)");
                tvDisease.setVisibility(View.VISIBLE);
            }
        }
    }

    private void switchService(ServiceType type) {
        currentType = type;
        
        // Reset backgrounds
        cardVetIcon.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
        cardGroomIcon.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
        cardBoardingIcon.setCardBackgroundColor(Color.parseColor("#E0E0E0"));

        // Set active background and content
        switch (type) {
            case VETERINARY:
                cardVetIcon.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
                layoutFindDisease.setVisibility(View.VISIBLE);
                tvNearbyTitle.setText("Nearby Veterinarian");
                tvRecommendedTitle.setText("Recommended Veterinarian");
                fetchServices("Veterinarian");
                break;
            case GROOMING:
                cardGroomIcon.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
                layoutFindDisease.setVisibility(View.GONE);
                tvNearbyTitle.setText("Nearby Grooming room");
                tvRecommendedTitle.setText("Recommended Grooming room");
                fetchServices("Grooming");
                break;
            case BOARDING:
                cardBoardingIcon.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
                layoutFindDisease.setVisibility(View.GONE);
                tvNearbyTitle.setText("Nearby Boarding");
                tvRecommendedTitle.setText("Recommended Boarding");
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
                    if (!role.equalsIgnoreCase(cachedRole)) {
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role).apply();
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

                        ServiceAdapter adapter = new ServiceAdapter(PetServicesActivity.this, list);
                        rvNearby.setAdapter(adapter);
                        rvRecommended.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }
}