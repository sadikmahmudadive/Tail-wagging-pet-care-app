package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPetsActivity extends AppCompatActivity implements PetAdapter.OnPetListener {

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> petList;
    private LinearLayout layoutNoPets; // Changed from TextView to LinearLayout
    private FloatingActionButton fabAddPet;
    private ImageButton buttonBack;
    // private TextView topBarTitle; // If you need to change the title dynamically

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference petsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Usually not needed with CoordinatorLayout managing fitsSystemWindows
        setContentView(R.layout.activity_my_pets);

        // Initialize views from the new layout
        buttonBack = findViewById(R.id.buttonBack);
        // topBarTitle = findViewById(R.id.topBarTitle); // Uncomment if you need to access it

        recyclerViewPets = findViewById(R.id.recyclerViewPets);
        layoutNoPets = findViewById(R.id.layoutNoPets); // Reference to the LinearLayout
        fabAddPet = findViewById(R.id.fabAddPet);

        // Setup RecyclerView
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        // Use the PetAdapter constructor that takes the OnPetListener
        petAdapter = new PetAdapter(this, petList, this);
        recyclerViewPets.setAdapter(petAdapter);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        // Set Click Listeners
        buttonBack.setOnClickListener(v -> onBackPressed()); // Or finish();

        fabAddPet.setOnClickListener(v -> {
            Intent intent = new Intent(MyPetsActivity.this, AddEditPet.class);
            startActivity(intent);
        });

        loadPets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPets(); // Refresh list when activity resumes
    }

    private void loadPets() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            layoutNoPets.setVisibility(View.VISIBLE);
            recyclerViewPets.setVisibility(View.GONE);
            return;
        }

        // Consider adding a ProgressBar while loading
        petsRef.orderByChild("ownerID").equalTo(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear(); // Clear previous pet data
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Pet pet = snapshot.getValue(Pet.class);
                        if (pet != null) {
                            petList.add(pet);
                        }
                    }
                    petAdapter.updatePets(petList); // Use the adapter's update method

                    if (petList.isEmpty()) {
                        layoutNoPets.setVisibility(View.VISIBLE);
                        recyclerViewPets.setVisibility(View.GONE);
                    } else {
                        layoutNoPets.setVisibility(View.GONE);
                        recyclerViewPets.setVisibility(View.VISIBLE);
                    }
                } else {
                    // No pets node exists for this user or at all for this query
                    petAdapter.updatePets(new ArrayList<>()); // Ensure adapter is cleared
                    layoutNoPets.setVisibility(View.VISIBLE);
                    recyclerViewPets.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyPetsActivity.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                layoutNoPets.setVisibility(View.VISIBLE);
                recyclerViewPets.setVisibility(View.GONE);
                // Consider logging the error for debugging
            }
        });
    }

    @Override
    public void onPetLongClick(Pet pet) {
        Intent intent = new Intent(this, PetDetailsActivity.class);
        intent.putExtra("SELECTED_PET", pet); // Pet class must be Parcelable
        startActivity(intent);
    }
}