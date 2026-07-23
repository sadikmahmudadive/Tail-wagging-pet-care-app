package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    ImageButton logoutButton;
    TextView textView, tvWelcomeMessage;
    ImageView userProfilePhoto;

    private DatabaseReference dbRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PetAdapter petAdapterHorizontal; // Keep a reference if needed elsewhere, or make local
    private TodayEventAdapter todayEventAdapter;
    private Double userLat, userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        // Ensure notification listener is running
        if (user != null) {
            ((App) getApplication()).startNotificationListener();
        }

        textView = findViewById(R.id.appBarUserName);
        tvWelcomeMessage = findViewById(R.id.appBarGreeting);
        userProfilePhoto = findViewById(R.id.appBarProfilePhoto);

        setDynamicWelcomeMessage();
        requestLocationPermission();

        NavbarHelper.setupNavbar(this);

        // New: Click listener for My Pets navigation (if you add a dedicated button for it)
        Button navMyPets = findViewById(R.id.btnViewAllPets); // Assuming you add an ID navMyPets
        if (navMyPets != null) {
            navMyPets.setOnClickListener(v -> launchMyPetsActivity());
        }

        // Connect Track and Check buttons
        Button btnTrackPets = findViewById(R.id.btnTrackPets);
        if (btnTrackPets != null) {
            btnTrackPets.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PetTrackerActivity.class));
            });
        }

        Button btnCheckPets = findViewById(R.id.btnCheckPets);
        if (btnCheckPets != null) {
            btnCheckPets.setOnClickListener(v -> launchPetHealthActivity());
        }

        Button btnGoToShop = findViewById(R.id.btnGoToShop);
        if (btnGoToShop != null) {
            btnGoToShop.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            });
        }

        Button btnGoToCommunity = findViewById(R.id.btnGoToCommunity);
        if (btnGoToCommunity != null) {
            btnGoToCommunity.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CommunityActivity.class));
            });
        }

        View btnViewAllEvents = findViewById(R.id.btnViewAllEvents);
        if (btnViewAllEvents != null) {
            btnViewAllEvents.setOnClickListener(v -> launchCalendarActivity());
        }

        View btnViewAllVets = findViewById(R.id.btnViewAllVets);
        if (btnViewAllVets != null) {
            btnViewAllVets.setOnClickListener(v -> launchPetHealthActivity());
        }

        ImageButton btnNotifications = findViewById(R.id.appBarNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            });
        }

        ImageButton btnCart = findViewById(R.id.appBarCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            });
        }



        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            textView.setText("Guest");
            userProfilePhoto.setImageResource(R.drawable.ic_profile);
            // Potentially disable pet fetching or show an appropriate message
            return;
        }

        fetchAndDisplayUserData();

        if (user != null && "admin@mail.com".equalsIgnoreCase(user.getEmail())) {
            Toast.makeText(this, "Admin Mode: User View", Toast.LENGTH_SHORT).show();
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                fetchAndShowRegisteredPets();
                showUpcomingEvents();
            });
        }

        fetchAndShowRegisteredPets();
        showUpcomingEvents();
        showTopVeterinarians();
    }

    private void showTopVeterinarians() {
        RecyclerView recyclerViewVets = findViewById(R.id.recyclerViewVets);
        if (recyclerViewVets == null) return;

        recyclerViewVets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        dbRef.child("users").orderByChild("role").equalTo("Veterinarian")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Vet> vetList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Extract data from User profile to create Vet object
                            String name = snapshot.child("name").getValue(String.class);
                            String qualification = snapshot.child("qualification").getValue(String.class);
                            String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String vetId = snapshot.getKey();
                            
                            Double vLat = snapshot.child("latitude").getValue(Double.class);
                            Double vLng = snapshot.child("longitude").getValue(Double.class);
                            
                            if (qualification == null) qualification = "Registered Veterinarian";
                            
                            // Mocking some professional stats for now until Vets set them up
                            float rating = 4.5f;
                            int reviews = 10;
                            String tag = "Professional";
                            
                            String distance = "Local";
                            if (userLat != null && userLng != null && vLat != null && vLng != null) {
                                float[] results = new float[1];
                                Location.distanceBetween(userLat, userLng, vLat, vLng, results);
                                distance = String.format(Locale.getDefault(), "%.1f km", results[0] / 1000);
                            }

                            String experience = "Exp: 5+ years";
                            String lastVisit = "N/A";
                            int imageRes = R.drawable.ic_profile;
                            
                            Boolean isVerified = snapshot.child("isVerified").getValue(Boolean.class);

                            Vet vet = new Vet(vetId, name, qualification, rating, reviews, tag, distance, "N/A", experience, lastVisit, imageRes);
                            if (isVerified != null) vet.setVerified(isVerified);

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                vet.setImageUrl(photoUrl);
                            }
                            if (phone != null) {
                                vet.setPhone(phone);
                            }
                            vetList.add(vet);
                        }
                        
                        if (vetList.isEmpty()) {
                            // Fallback mock if no vets registered yet for UI testing
                            Vet mockVet = new Vet("mock_id", "Dr. Nambuvan", "Bachelor of veterinary science", 5.0f, 100, "Expert", "2.5 km", "1000 Tk", "10 years", "25/11/2022", R.drawable.pet1);
                            mockVet.setBio("Dr. Shehan, one of the most skilled and experienced veterinarians and the owner of the most convenient animal clinic \"Petz & Vetz\". Our paradise is situated in the heart of the town with a pleasant environment. We are ready to treat your beloved doggos & puppers with love and involvement.");
                            mockVet.setRecommendedFor("Bella");
                            vetList.add(mockVet);
                        }

                        VetAdapter vetAdapter = new VetAdapter(MainActivity.this, vetList);
                        recyclerViewVets.setAdapter(vetAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Show mock data if database fails or no connection for testing
                        List<Vet> vetList = new ArrayList<>();
                        Vet mockVet = new Vet("mock_id", "Dr. Nambuvan", "Bachelor of veterinary science", 5.0f, 100, "Expert", "2.5 km", "1000 Tk", "10 years", "25/11/2022", R.drawable.pet1);
                        mockVet.setBio("Dr. Shehan, one of the most skilled and experienced veterinarians and the owner of the most convenient animal clinic \"Petz & Vetz\". Our paradise is situated in the heart of the town with a pleasant environment.");
                        mockVet.setRecommendedFor("Bella");
                        vetList.add(mockVet);
                        VetAdapter vetAdapter = new VetAdapter(MainActivity.this, vetList);
                        recyclerViewVets.setAdapter(vetAdapter);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavbarHelper.refresh(this);
        if (user != null) {
            updateFcmToken(user.getUid());
        }
        fetchAndDisplayUserData();
        fetchAndShowRegisteredPets();
        showUpcomingEvents();
        showTopVeterinarians();
    }

    private void updateFcmToken(String uid) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        dbRef.child("users").child(uid).child("fcmToken").setValue(token);
                    }
                });
    }

    private void showUpcomingEvents() {
        RecyclerView recyclerViewPetEvents = findViewById(R.id.recyclerViewPetEvents);
        if (recyclerViewPetEvents == null) return;

        recyclerViewPetEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        // Get all events from today onwards
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Event> events = EventStore.getInstance(this).getUpcomingEvents(today);
        
        todayEventAdapter = new TodayEventAdapter(this, events, () -> {
            // Refresh if an event is deleted from the dashboard
            showUpcomingEvents();
        });
        todayEventAdapter.setHorizontal(true);
        recyclerViewPetEvents.setAdapter(todayEventAdapter);
    }

    private void setDynamicWelcomeMessage() {
        if (tvWelcomeMessage == null) return;
        
        java.util.Calendar c = java.util.Calendar.getInstance();
        int timeOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 5 && timeOfDay < 12) {
            tvWelcomeMessage.setText(R.string.good_morning);
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            tvWelcomeMessage.setText(R.string.good_afternoon);
        } else if (timeOfDay >= 17 && timeOfDay < 21) {
            tvWelcomeMessage.setText(R.string.good_evening);
        } else {
            tvWelcomeMessage.setText(R.string.good_night);
        }
    }

    private void requestLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (android.os.Build.VERSION.SDK_INT >= 33) { // TIRAMISU
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    permissions.add("android.permission.POST_NOTIFICATIONS");
                }
            }

            if (!permissions.isEmpty()) {
                androidx.core.app.ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 102);
            }
        }
    }

    private void launchCalendarActivity() {
        Intent intent = new Intent(MainActivity.this, Calendar.class);
        startActivity(intent);
    }

    private void launchAddEditPetActivity() {
        Intent intent = new Intent(MainActivity.this, AddEditPet.class);
        startActivity(intent);
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(MainActivity.this, Profile.class);
        startActivity(intent);
    }

    private void launchPetHealthActivity() {
        Intent intent = new Intent(MainActivity.this, PetServicesActivity.class);
        startActivity(intent);
    }

    // New method to launch MyPetsActivity
    private void launchMyPetsActivity() {
        Intent intent = new Intent(MainActivity.this, MyPetsActivity.class);
        startActivity(intent);
    }

    private void fetchAndDisplayUserData() {
        if (user == null) return;

        if (!NetworkUtils.isNetworkAvailable(this)) {
            // If offline, try to load from cached display name/photo
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                textView.setText(displayName);
            }
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(userProfilePhoto);
            }
        }

        // Always try to fetch from Realtime DB to ensure role and other fields are synced
        fetchUserFieldsFromRealtimeDatabase();
    }

    private void fetchUserFieldsFromRealtimeDatabase() {
        if (user == null) return; // Guard clause

        dbRef.child("users").child(user.getUid())
                .addValueEventListener(new ValueEventListener() { // Changed to addValueEventListener for live points update
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("name").getValue(String.class);
                            String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
                            Long points = dataSnapshot.child("points").getValue(Long.class);
                            
                            TextView tvUserPoints = findViewById(R.id.tvUserPoints);
                            if (tvUserPoints != null && points != null) {
                                tvUserPoints.setText(String.valueOf(points));
                            }
                            
                            // Ensure referral code exists for legacy users
                            if (!dataSnapshot.hasChild("referralCode")) {
                                String generatedCode = user.getUid().substring(0, 6).toUpperCase();
                                dbRef.child("users").child(user.getUid()).child("referralCode").setValue(generatedCode);
                                dbRef.child("users").child(user.getUid()).child("points").setValue(points == null ? 15 : points);
                            }
                            
                            userLat = dataSnapshot.child("latitude").getValue(Double.class);
                            userLng = dataSnapshot.child("longitude").getValue(Double.class);
                            
                            if (username != null && !username.isEmpty()) {
                                textView.setText(username);
                            } else {
                                // Fallback if name is also empty in DB
                                textView.setText(user.getEmail() != null ? user.getEmail() : "Unknown User");
                            }

                            String role = dataSnapshot.child("role").getValue(String.class);
                            if (role != null) {
                                String cachedRole = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_role", "Pet Owner");
                                if (!role.trim().equalsIgnoreCase(cachedRole.trim())) {
                                    getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role.trim()).apply();
                                    NavbarHelper.setupNavbar(MainActivity.this);
                                }
                            }

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(MainActivity.this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(userProfilePhoto);
                            } else {
                                userProfilePhoto.setImageResource(R.drawable.ic_profile); // Default if no photoUrl in DB
                            }
                            
                            // Re-fetch vets now that we have user location
                            showTopVeterinarians();
                        } else {
                            textView.setText(user.getEmail() != null ? user.getEmail() : "User Data Not Found");
                            userProfilePhoto.setImageResource(R.drawable.ic_profile);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Failed to fetch user data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        userProfilePhoto.setImageResource(R.drawable.ic_profile);
                    }
                });
    }

    private void fetchAndShowRegisteredPets() {
        RecyclerView recyclerViewPets = findViewById(R.id.recyclerViewPets);
        if (recyclerViewPets == null) return;

        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FrameLayout petsProgressOverlay = findViewById(R.id.petsProgressOverlay);
        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.VISIBLE);

        if (user == null) {
            if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.GONE);
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Use addValueEventListener for real-time updates when a new pet is added
        dbRef.child("pets")
                .orderByChild("ownerID").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                        List<Pet> petList = new ArrayList<>();
                        for (DataSnapshot petSnap : dataSnapshot.getChildren()) {
                            Pet pet = petSnap.getValue(Pet.class);
                            if (pet != null) {
                                pet.setPetID(petSnap.getKey());
                                petList.add(pet);
                            }
                        }
                        
                        // Handle empty state for pets
                        View cardEmptyPets = findViewById(R.id.cardEmptyPets);
                        if (cardEmptyPets != null) {
                            if (petList.isEmpty()) {
                                cardEmptyPets.setVisibility(View.VISIBLE);
                                recyclerViewPets.setVisibility(View.GONE);
                                cardEmptyPets.setOnClickListener(v -> launchAddEditPetActivity());
                            } else {
                                cardEmptyPets.setVisibility(View.GONE);
                                recyclerViewPets.setVisibility(View.VISIBLE);
                            }
                        }

                        // Create/Update adapter
                        if (petAdapterHorizontal == null) {
                            petAdapterHorizontal = new PetAdapter(MainActivity.this, petList);
                            petAdapterHorizontal.setOnPetSimpleClickListener(clickedPet -> launchMyPetsActivity());
                            recyclerViewPets.setAdapter(petAdapterHorizontal);
                        } else {
                            petAdapterHorizontal.updatePets(petList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
