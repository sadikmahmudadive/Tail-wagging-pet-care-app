package com.example.tailwagging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VetDetailsActivity extends AppCompatActivity {

    private Vet selectedVet;
    private DatabaseReference dbRef;
    private RecyclerView rvReviews;
    private TextView tvNoReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_details);

        selectedVet = (Vet) getIntent().getSerializableExtra("SELECTED_VET");
        if (selectedVet == null) {
            Toast.makeText(this, "Vet details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderName = findViewById(R.id.tvHeaderVetName);
        
        com.google.android.material.appbar.AppBarLayout appBarLayout = findViewById(R.id.appBar);
        appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) -> {
            float percentage = (float) Math.abs(verticalOffset) / layout.getTotalScrollRange();
            if (percentage >= 0.7f) {
                tvHeaderName.setAlpha(1f);
            } else {
                tvHeaderName.setAlpha(0f);
            }
        });

        ImageView ivMain = findViewById(R.id.ivVetDetailMain);
        TextView tvName = findViewById(R.id.tvVetNameDetail);
        TextView tvQual = findViewById(R.id.tvVetQualificationDetail);
        TextView tvRating = findViewById(R.id.tvRatingScore);
        RatingBar rb = findViewById(R.id.ratingBarVet);
        TextView tvReviews = findViewById(R.id.tvReviewsCount);
        TextView tvHours = findViewById(R.id.tvBusinessHours);
        TextView tvDist = findViewById(R.id.tvVetDistanceDetail);
        TextView tvPhone = findViewById(R.id.tvVetPhoneDetail);
        TextView tvBio = findViewById(R.id.tvVetBio);
        TextView tvRecPet = findViewById(R.id.tvRecommendedPetName);
        Button btnBook = findViewById(R.id.btnBookAppointmentDetail);
        
        Button btnWriteReview = findViewById(R.id.btnWriteReview);

        tvHeaderName.setText(selectedVet.getName());
        tvName.setText(selectedVet.getName());
        tvQual.setText(selectedVet.getQualification());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", selectedVet.getRating()));
        rb.setRating(selectedVet.getRating());
        tvReviews.setText(String.format(Locale.getDefault(), "(%d reviews)", selectedVet.getReviewsCount()));
        
        View reviewsArea = findViewById(R.id.layoutReviewsHeader);
        if (reviewsArea == null) reviewsArea = tvReviews;
        
        reviewsArea.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReviewsActivity.class);
            intent.putExtra("VET_ID", selectedVet.getId());
            intent.putExtra("VET_NAME", selectedVet.getName());
            startActivity(intent);
        });

        tvHours.setText(selectedVet.getBusinessHours());
        tvDist.setText(selectedVet.getDistance());
        tvPhone.setText(selectedVet.getPhone() != null ? selectedVet.getPhone() : "Contact not provided");
        if (selectedVet.getPhone() != null) {
            tvPhone.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + selectedVet.getPhone()));
                startActivity(intent);
            });
        }
        tvBio.setText(selectedVet.getBio());
        tvRecPet.setText(selectedVet.getRecommendedFor());

        findViewById(R.id.ivVerifiedDetail).setVisibility(selectedVet.isVerified() ? View.VISIBLE : View.GONE);

        if (selectedVet.getImageUrl() != null && !selectedVet.getImageUrl().isEmpty()) {
            Glide.with(this).load(selectedVet.getImageUrl()).placeholder(R.drawable.ic_profile).into(ivMain);
        } else {
            Glide.with(this).load(selectedVet.getImageResId()).into(ivMain);
        }

        btnBack.setOnClickListener(v -> finish());
        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("VET_ID", selectedVet.getId());
            intent.putExtra("VET_NAME", selectedVet.getName());
            startActivity(intent);
        });

        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());

        rvReviews = findViewById(R.id.rvReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setAdapter(reviewAdapter);

        fetchReviews();

        ImageButton btnFav = findViewById(R.id.btnFavorite);
        checkIfFavorite(btnFav);
        btnFav.setOnClickListener(v -> toggleFavorite(btnFav));

        NavbarHelper.setupNavbar(this);
    }

    private void fetchReviews() {
        dbRef.child("reviews").orderByChild("vetId").equalTo(selectedVet.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Review review = ds.getValue(Review.class);
                            if (review != null) reviewList.add(review);
                        }
                        Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.timestamp, r1.timestamp));
                        reviewAdapter.notifyDataSetChanged();
                        tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkIfFavorite(ImageButton btnFav) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        dbRef.child("users").child(uid).child("favorites").child(selectedVet.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnFav.setColorFilter(getColor(R.color.orange));
                            btnFav.setTag("fav");
                        } else {
                            btnFav.setColorFilter(getColor(R.color.dark_blue));
                            btnFav.setTag("not_fav");
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void toggleFavorite(ImageButton btnFav) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Login to favorite", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference favRef = dbRef.child("users").child(uid).child("favorites").child(selectedVet.getId());
        if ("fav".equals(btnFav.getTag())) {
            favRef.removeValue().addOnSuccessListener(aVoid -> {
                btnFav.setColorFilter(getColor(R.color.dark_blue));
                btnFav.setTag("not_fav");
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            });
        } else {
            favRef.setValue(true).addOnSuccessListener(aVoid -> {
                btnFav.setColorFilter(getColor(R.color.orange));
                btnFav.setTag("fav");
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showWriteReviewDialog() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Please login to write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        RatingBar rb = dialogView.findViewById(R.id.rbWriteReview);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> {
            float rating = rb.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            dbRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String photo = snapshot.child("photoUrl").getValue(String.class);
                    
                    String reviewId = dbRef.child("reviews").push().getKey();
                    Review newReview = new Review(
                            reviewId,
                            currentUserId,
                            name != null ? name : "User",
                            photo,
                            selectedVet.getId(),
                            rating,
                            comment,
                            System.currentTimeMillis()
                    );

                    if (reviewId != null) {
                        dbRef.child("reviews").child(reviewId).setValue(newReview)
                                .addOnSuccessListener(aVoid -> {
                                    updateVetOverallRating(rating);
                                    Toast.makeText(VetDetailsActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        dialog.show();
    }

    private void updateVetOverallRating(float newRating) {
        dbRef.child("users").child(selectedVet.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float currentRating = 0;
                int currentCount = 0;
                
                Object ratingVal = snapshot.child("rating").getValue();
                if (ratingVal != null) {
                    currentRating = ((Number) ratingVal).floatValue();
                }
                
                Integer countVal = snapshot.child("reviewsCount").getValue(Integer.class);
                if (countVal != null) {
                    currentCount = countVal;
                }

                int newCount = currentCount + 1;
                float newAverage = ((currentRating * currentCount) + newRating) / newCount;

                Map<String, Object> updates = new HashMap<>();
                updates.put("rating", newAverage);
                updates.put("reviewsCount", newCount);

                dbRef.child("users").child(selectedVet.getId()).updateChildren(updates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}