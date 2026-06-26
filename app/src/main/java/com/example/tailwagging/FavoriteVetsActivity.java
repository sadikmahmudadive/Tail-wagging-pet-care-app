package com.example.tailwagging;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteVetsActivity extends AppCompatActivity {

    private RecyclerView rvFavVets;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutNoFavs;
    private DatabaseReference dbRef;
    private String userId;
    private List<Vet> favVetList = new ArrayList<>();
    private VetAdapter adapter;
    private Double userLat, userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_vets);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        rvFavVets = findViewById(R.id.rvFavVets);
        swipeRefresh = findViewById(R.id.swipeRefreshFav);
        layoutNoFavs = findViewById(R.id.layoutNoFavs);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvFavVets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VetAdapter(this, favVetList);
        rvFavVets.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchUserLocationAndFavorites);

        fetchUserLocationAndFavorites();
        NavbarHelper.setupNavbar(this);
    }

    private void fetchUserLocationAndFavorites() {
        if (userId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLat = snapshot.child("latitude").getValue(Double.class);
                userLng = snapshot.child("longitude").getValue(Double.class);
                fetchFavorites();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchFavorites();
            }
        });
    }

    private void fetchFavorites() {
        dbRef.child("users").child(userId).child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favVetList.clear();
                if (!snapshot.exists()) {
                    updateUI();
                    return;
                }

                int totalFavs = (int) snapshot.getChildrenCount();
                final int[] loadedCount = {0};

                for (DataSnapshot favSnap : snapshot.getChildren()) {
                    String vetId = favSnap.getKey();
                    if (vetId != null) {
                        dbRef.child("users").child(vetId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot vetSnap) {
                                if (vetSnap.exists()) {
                                    String name = vetSnap.child("name").getValue(String.class);
                                    String qualification = vetSnap.child("qualification").getValue(String.class);
                                    String photoUrl = vetSnap.child("photoUrl").getValue(String.class);
                                    String phone = vetSnap.child("phone").getValue(String.class);
                                    
                                    Double vLat = vetSnap.child("latitude").getValue(Double.class);
                                    Double vLng = vetSnap.child("longitude").getValue(Double.class);
                                    
                                    String distance = "Local";
                                    if (userLat != null && userLng != null && vLat != null && vLng != null) {
                                        float[] results = new float[1];
                                        Location.distanceBetween(userLat, userLng, vLat, vLng, results);
                                        distance = String.format(Locale.getDefault(), "%.1f km", results[0] / 1000);
                                    }

                                    float rating = 4.5f;
                                    Object rVal = vetSnap.child("rating").getValue();
                                    if (rVal != null) rating = ((Number) rVal).floatValue();

                                    int reviews = 0;
                                    Integer cVal = vetSnap.child("reviewsCount").getValue(Integer.class);
                                    if (cVal != null) reviews = cVal;

                                    Boolean isVerified = vetSnap.child("isVerified").getValue(Boolean.class);

                                    Vet vet = new Vet(vetId, name, qualification, rating, reviews, "Favorite", distance, "N/A", "N/A", "N/A", R.drawable.ic_profile);
                                    if (isVerified != null) vet.setVerified(isVerified);

                                    if (photoUrl != null && !photoUrl.isEmpty()) vet.setImageUrl(photoUrl);
                                    if (phone != null) vet.setPhone(phone);
                                    favVetList.add(vet);
                                }
                                loadedCount[0]++;
                                if (loadedCount[0] == totalFavs) {
                                    updateUI();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loadedCount[0]++;
                                if (loadedCount[0] == totalFavs) {
                                    updateUI();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateUI();
            }
        });
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);
        adapter.notifyDataSetChanged();
        layoutNoFavs.setVisibility(favVetList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}