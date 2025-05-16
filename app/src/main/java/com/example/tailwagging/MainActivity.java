package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView; // <-- Add this import
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

import com.bumptech.glide.Glide; // <-- Add this import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    Button logoutButton;
    TextView textView;
    FirebaseFirestore db;
    ImageView userProfilePhoto; // <-- Add this line

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
        db = FirebaseFirestore.getInstance();

        logoutButton = findViewById(R.id.buttonLogout);
        textView = findViewById(R.id.userName);
        userProfilePhoto = findViewById(R.id.userProfilePhoto); // <-- Add this line

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
            // Optionally set a default image
            userProfilePhoto.setImageResource(R.drawable.ic_profile);
            return;
        }

        // Fetch and display user data
        fetchAndDisplayUserData();
    }

    private void launchCalendarActivity() {
        Intent intent = new Intent(MainActivity.this, Calendar.class); // <-- Use your custom calendar class here
        startActivity(intent);
    }

    // New method to launch AddEditPetActivity
    private void launchAddEditPetActivity() {
        Intent intent = new Intent(MainActivity.this, AddEditPet.class); // Make sure this class exists
        startActivity(intent);
    }

    // New method to launch ProfileActivity
    private void launchProfileActivity() {
        Intent intent = new Intent(MainActivity.this, Profile.class); // Make sure this class exists
        startActivity(intent);
    }

    private void fetchAndDisplayUserData() {
        // Get user data from Firebase Authentication
        String displayName = user.getDisplayName();

        // If display name is available, set it in the TextView
        if (displayName != null && !displayName.isEmpty()) {
            textView.setText(displayName);
        } else {
            // Fetch display name and profile photo from Firestore if not available in Firebase Auth
            fetchUserFieldsFromFirestore();
        }
    }

    private void fetchUserFieldsFromFirestore() {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("name");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                if (username != null && !username.isEmpty()) {
                    textView.setText(username);
                } else {
                    textView.setText("Unknown User");
                }
                // Fetch and display profile photo
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profileImageUrl)
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
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            userProfilePhoto.setImageResource(R.drawable.ic_profile);
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