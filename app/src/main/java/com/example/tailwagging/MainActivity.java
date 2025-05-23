package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    Button logoutButton;
    TextView textView;
    ImageView userProfilePhoto;

    // Realtime Database reference with custom URL
    private DatabaseReference dbRef;

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
        // Use your custom Firebase Realtime Database URL
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-c24fa-default-rtdb.firebaseio.com/").getReference();

        logoutButton = findViewById(R.id.buttonLogout);
        textView = findViewById(R.id.userName);
        userProfilePhoto = findViewById(R.id.userProfilePhoto);

        // --- NAVIGATION BAR:
        // CALENDAR ---
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        navCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCalendarActivity();
            }
        });

        // --- NAVIGATION BAR:
        // ADD PET ---
        LinearLayout navAddPet = findViewById(R.id.navAddPet);
        navAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddEditPetActivity();
            }
        });

        // --- NAVIGATION BAR:
        // PROFILE ---
        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchProfileActivity();
            }
        });

        // Logout functionality with confirmation
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Check if user is logged in
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            textView.setText("Guest");
            userProfilePhoto.setImageResource(R.drawable.ic_profile);
            return;
        }

        // Fetch and display user data
        fetchAndDisplayUserData();
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

    private void fetchAndDisplayUserData() {
        // Get user data from Firebase Authentication
        String displayName = user.getDisplayName();

        // If display name is available, set it in the TextView
        if (displayName != null && !displayName.isEmpty()) {
            textView.setText(displayName);

            // Also try to display photoUrl from Auth if available
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(userProfilePhoto);
            } else {
                // fallback to database
                fetchUserFieldsFromRealtimeDatabase();
            }
        } else {
            // Fetch display name and profile photo from Realtime Database if not available in Firebase Auth
            fetchUserFieldsFromRealtimeDatabase();
        }
    }

    private void fetchUserFieldsFromRealtimeDatabase() {
        dbRef.child("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("name").getValue(String.class);
                            String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
                            if (username != null && !username.isEmpty()) {
                                textView.setText(username);
                            } else {
                                textView.setText("Unknown User");
                            }
                            // Fetch and display profile photo
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(MainActivity.this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(userProfilePhoto);
                            } else {
                                userProfilePhoto.setImageResource(R.drawable.ic_profile);
                            }
                        } else {
                            textView.setText("User Not Found");
                            userProfilePhoto.setImageResource(R.drawable.ic_profile);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Failed to fetch user data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        userProfilePhoto.setImageResource(R.drawable.ic_profile);
                    }
                });

        // Set up RecyclerView for pets
        RecyclerView recyclerViewPets = findViewById(R.id.recyclerViewPets);
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //spinner
        FrameLayout petsProgressOverlay = findViewById(R.id.petsProgressOverlay);
        // Show while loading:
        petsProgressOverlay.setVisibility(View.VISIBLE);
        // Hide when done loading:
        petsProgressOverlay.setVisibility(View.GONE);
    }
}