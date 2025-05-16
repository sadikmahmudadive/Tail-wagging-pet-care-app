package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userEmail, userPhoneNumber;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the About Me option
        LinearLayout optionAboutMe = findViewById(R.id.optionAboutMe);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Reference views
        profileImage = findViewById(R.id.profileImage);
        userEmail = findViewById(R.id.userEmail);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);

        // Set data
        if (user != null) {
            // Set email
            userEmail.setText(user.getEmail());

            // Fetch phone and profile image URL from Firestore
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String phone = documentSnapshot.getString("phone");
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    if (phone != null && !phone.isEmpty()) {
                        userPhoneNumber.setText(phone);
                    } else {
                        userPhoneNumber.setText("No phone number");
                    }

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            userEmail.setText("No user");
            userPhoneNumber.setText("No phone number");
            profileImage.setImageResource(R.drawable.ic_profile);
        }

        optionAboutMe.setOnClickListener(v -> {
            // Launch EditUserProfileActivity as a pop-up dialog style
            Intent intent = new Intent(Profile.this, EditUserProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}