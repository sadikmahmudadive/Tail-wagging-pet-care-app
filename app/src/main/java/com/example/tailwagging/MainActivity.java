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

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    Button logoutButton;
    TextView textView;
    ImageView userProfilePhoto;

    private DatabaseReference dbRef;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        logoutButton = findViewById(R.id.buttonLogout);
        textView = findViewById(R.id.userName);
        userProfilePhoto = findViewById(R.id.userProfilePhoto);

        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> launchCalendarActivity());
        }

        LinearLayout navAddPet = findViewById(R.id.navAddPet);
        if (navAddPet != null) {
            navAddPet.setOnClickListener(v -> launchAddEditPetActivity());
        }

        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> launchProfileActivity());
        }

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

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            textView.setText("Guest");
            userProfilePhoto.setImageResource(R.drawable.ic_profile);
            return;
        }

        fetchAndDisplayUserData();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchAndShowRegisteredPets();
        });

        fetchAndShowRegisteredPets();
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
        String displayName = user.getDisplayName();

        if (displayName != null && !displayName.isEmpty()) {
            textView.setText(displayName);
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(userProfilePhoto);
            } else {
                fetchUserFieldsFromRealtimeDatabase();
            }
        } else {
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
    }

    private void fetchAndShowRegisteredPets() {
        RecyclerView recyclerViewPets = findViewById(R.id.recyclerViewPets);
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FrameLayout petsProgressOverlay = findViewById(R.id.petsProgressOverlay);
        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.VISIBLE);

        dbRef.child("pets")
                .orderByChild("ownerID").equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        List<Pet> petList = new ArrayList<>();
                        for (DataSnapshot petSnap : dataSnapshot.getChildren()) {
                            Pet pet = petSnap.getValue(Pet.class);
                            if (pet != null) petList.add(pet);
                        }
                        PetAdapter adapter = new PetAdapter(MainActivity.this, petList);
                        recyclerViewPets.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (petsProgressOverlay != null) petsProgressOverlay.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}