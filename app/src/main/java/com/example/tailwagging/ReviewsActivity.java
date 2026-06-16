package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReviewsActivity extends AppCompatActivity {

    private String vetId, vetName;
    private TextView tvAverage, tvTotalReviews;
    private RatingBar rbAverage;
    private ProgressBar pb5, pb4, pb3, pb2, pb1;
    private RecyclerView rvReviews;
    private DetailedReviewAdapter adapter;
    private final List<Review> reviewList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        vetId = getIntent().getStringExtra("VET_ID");
        vetName = getIntent().getStringExtra("VET_NAME");

        if (vetId == null) {
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        tvAverage = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviewsLabel);
        rbAverage = findViewById(R.id.rbAverage);
        
        pb5 = findViewById(R.id.pbExcellent);
        pb4 = findViewById(R.id.pbGood);
        pb3 = findViewById(R.id.pbAverage);
        pb2 = findViewById(R.id.pbBelowAverage);
        pb1 = findViewById(R.id.pbPoor);

        rvReviews = findViewById(R.id.rvDetailedReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetailedReviewAdapter(this, reviewList);
        rvReviews.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddReviewFloat).setOnClickListener(v -> showWriteReviewDialog());

        NavbarHelper.setupNavbar(this);
        fetchReviews();
    }

    private void fetchReviews() {
        dbRef.child("reviews").orderByChild("vetId").equalTo(vetId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        int count5 = 0, count4 = 0, count3 = 0, count2 = 0, count1 = 0;
                        float totalRating = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Review r = ds.getValue(Review.class);
                            if (r != null) {
                                reviewList.add(r);
                                totalRating += r.rating;
                                if (r.rating >= 4.5) count5++;
                                else if (r.rating >= 3.5) count4++;
                                else if (r.rating >= 2.5) count3++;
                                else if (r.rating >= 1.5) count2++;
                                else count1++;
                            }
                        }
                        Collections.reverse(reviewList);
                        adapter.notifyDataSetChanged();

                        updateSummary(totalRating, reviewList.size(), count5, count4, count3, count2, count1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateSummary(float totalRating, int totalCount, int c5, int c4, int c3, int c2, int c1) {
        if (totalCount == 0) {
            tvAverage.setText("0.0");
            rbAverage.setRating(0);
            tvTotalReviews.setText("No reviews yet");
            pb5.setProgress(0); pb4.setProgress(0); pb3.setProgress(0); pb2.setProgress(0); pb1.setProgress(0);
            return;
        }

        float avg = totalRating / totalCount;
        tvAverage.setText(String.format(Locale.getDefault(), "%.1f", avg));
        rbAverage.setRating(avg);
        tvTotalReviews.setText(String.format(Locale.getDefault(), "Based on %d reviews", totalCount));

        pb5.setProgress((c5 * 100) / totalCount);
        pb4.setProgress((c4 * 100) / totalCount);
        pb3.setProgress((c3 * 100) / totalCount);
        pb2.setProgress((c2 * 100) / totalCount);
        pb1.setProgress((c1 * 100) / totalCount);
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
                            vetId,
                            rating,
                            comment,
                            System.currentTimeMillis()
                    );

                    if (reviewId != null) {
                        dbRef.child("reviews").child(reviewId).setValue(newReview)
                                .addOnSuccessListener(aVoid -> {
                                    updateVetOverallRating(rating);
                                    Toast.makeText(ReviewsActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
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
        dbRef.child("users").child(vetId).addListenerForSingleValueEvent(new ValueEventListener() {
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

                dbRef.child("users").child(vetId).updateChildren(updates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}