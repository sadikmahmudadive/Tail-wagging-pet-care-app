package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private static final String TAG = "MyPetsActivity";

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> petList;
    private LinearLayout layoutNoPets;
    private AppCompatButton buttonBack;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference petsRef;
    private ValueEventListener petsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);

        Log.d(TAG, "onCreate called");

        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewPets = findViewById(R.id.recyclerViewPets);
        layoutNoPets = findViewById(R.id.layoutNoPets);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(this, new ArrayList<>(petList), this); // Defensive copy
        recyclerViewPets.setAdapter(petAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        petsRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("pets");

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Swipe-to-refresh triggered");
                loadPets();
            });
        }

        loadPets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    private void loadPets() {
        Log.d(TAG, "loadPets called");
        if (currentUser == null) {
            Log.w(TAG, "User not logged in");
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            showNoPetsLayout();
            setRefreshing(false);
            return;
        }

        Log.d(TAG, "Current user UID: " + currentUser.getUid());

        // Remove previous listener if present
        if (petsListener != null) {
            petsRef.removeEventListener(petsListener);
            Log.d(TAG, "Previous petsListener removed");
        }

        petsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange called, dataSnapshot.exists(): " + dataSnapshot.exists());
                Log.d(TAG, "Children count: " + dataSnapshot.getChildrenCount());

                List<Pet> loadedPets = new ArrayList<>();
                int petCount = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Pet pet = snapshot.getValue(Pet.class);
                        if (pet != null) {
                            loadedPets.add(pet);
                            Log.d(TAG, "Pet found: " + pet.getName() + ", ownerID: " + pet.getOwnerID() + ", petID: " + pet.getPetID());
                            petCount++;
                        } else {
                            Log.d(TAG, "Null pet object for key: " + snapshot.getKey());
                        }
                    }
                }
                Log.d(TAG, "Total pets loaded: " + petCount);

                // Update the petList & adapter (defensive copy)
                petList.clear();
                petList.addAll(loadedPets);
                if (petAdapter != null) {
                    petAdapter.updatePets(new ArrayList<>(petList));
                    Log.d(TAG, "PetAdapter updated, petList.size(): " + petList.size());
                }

                // UI logic
                if (petList.isEmpty()) {
                    Log.d(TAG, "No pets found, showing no-pets layout");
                    showNoPetsLayout();
                } else {
                    Log.d(TAG, "Pets found, showing recyclerViewPets");
                    showPetsRecyclerView();
                }
                setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
                Toast.makeText(MyPetsActivity.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                showNoPetsLayout();
                setRefreshing(false);
            }
        };

        Log.d(TAG, "Querying pets for ownerID: " + currentUser.getUid());
        petsRef.orderByChild("ownerID").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(petsListener);
        setRefreshing(true);
    }

    private void showNoPetsLayout() {
        if (layoutNoPets != null) layoutNoPets.setVisibility(View.VISIBLE);
        if (recyclerViewPets != null) recyclerViewPets.setVisibility(View.GONE);
    }

    private void showPetsRecyclerView() {
        if (layoutNoPets != null) layoutNoPets.setVisibility(View.GONE);
        if (recyclerViewPets != null) recyclerViewPets.setVisibility(View.VISIBLE);
    }

    private void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void onPetLongClick(Pet pet) {
        Log.d(TAG, "onPetLongClick: " + (pet != null ? pet.getName() : "null"));
        Intent intent = new Intent(this, PetDetailsActivity.class);
        intent.putExtra("SELECTED_PET", pet);
        startActivity(intent);
    }
}