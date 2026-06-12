package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class VetDetailsActivity extends AppCompatActivity {

    private Vet selectedVet;

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

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderName = findViewById(R.id.tvHeaderVetName);
        ImageView ivMain = findViewById(R.id.ivVetDetailMain);
        TextView tvName = findViewById(R.id.tvVetNameDetail);
        TextView tvQual = findViewById(R.id.tvVetQualificationDetail);
        TextView tvRating = findViewById(R.id.tvRatingScore);
        RatingBar rb = findViewById(R.id.ratingBarVet);
        TextView tvReviews = findViewById(R.id.tvReviewsCount);
        TextView tvHours = findViewById(R.id.tvBusinessHours);
        TextView tvDist = findViewById(R.id.tvVetDistanceDetail);
        TextView tvFee = findViewById(R.id.tvVetFeeDetail);
        TextView tvBio = findViewById(R.id.tvVetBio);
        TextView tvRecPet = findViewById(R.id.tvRecommendedPetName);
        Button btnBook = findViewById(R.id.btnBookAppointmentDetail);

        tvHeaderName.setText(selectedVet.getName());
        tvName.setText(selectedVet.getName());
        tvQual.setText(selectedVet.getQualification());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", selectedVet.getRating()));
        rb.setRating(selectedVet.getRating());
        tvReviews.setText(String.format(Locale.getDefault(), "(%d reviews)", selectedVet.getReviewsCount()));
        tvHours.setText(selectedVet.getBusinessHours());
        tvDist.setText(selectedVet.getDistance());
        tvFee.setText(String.format(Locale.getDefault(), "%s for an Appointment", selectedVet.getPrice()));
        tvBio.setText(selectedVet.getBio());
        tvRecPet.setText(selectedVet.getRecommendedFor());

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

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // IDs from layout_navigation_bar.xml
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        findViewById(R.id.navCalendar).setOnClickListener(v -> startActivity(new Intent(this, Calendar.class)));
        findViewById(R.id.navManage).setOnClickListener(v -> startActivity(new Intent(this, MyPetsActivity.class)));
        findViewById(R.id.navAddPet).setOnClickListener(v -> startActivity(new Intent(this, AddEditPet.class)));
        findViewById(R.id.navVet).setOnClickListener(v -> {
            // Already on vet details/sections, maybe just go back to home or top
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }
}