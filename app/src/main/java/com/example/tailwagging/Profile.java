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
// Realtime Database import
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userEmail, userPhoneNumber;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

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
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Reference views
        profileImage = findViewById(R.id.profileImage);
        userEmail = findViewById(R.id.userEmail);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);

        // Set data
        if (user != null) {
            // Set email
            userEmail.setText(user.getEmail());

            // Fetch phone and profile image URL from Realtime Database
            dbRef.child("users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String phone = dataSnapshot.child("phone").getValue(String.class);
                                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class); // <-- changed to photoUrl

                                if (phone != null && !phone.isEmpty()) {
                                    userPhoneNumber.setText(phone);
                                } else {
                                    userPhoneNumber.setText("No phone number");
                                }

                                if (photoUrl != null && !photoUrl.isEmpty()) {
                                    Glide.with(Profile.this)
                                            .load(photoUrl)
                                            .placeholder(R.drawable.ic_profile)
                                            .error(R.drawable.ic_profile)
                                            .into(profileImage);
                                } else {
                                    profileImage.setImageResource(R.drawable.ic_profile);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(Profile.this, "Failed to load profile: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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