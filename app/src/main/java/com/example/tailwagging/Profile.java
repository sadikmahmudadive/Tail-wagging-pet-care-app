package com.example.tailwagging;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Realtime Database import
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userEmail, userPhoneNumber;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    private Uri imageUri;

    private boolean cloudinaryInitialized = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImageToCloudinary(imageUri);
                    }
                }
            }
    );

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

        // Cloudinary init
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dhm0edatk");
            config.put("api_key", "879315316647413");
            config.put("api_secret", "BgrjuKuPR_UqGZf2Gb5RHKDmF_0");
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }

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
                                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

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

        profileImage.setOnClickListener(v -> openImagePicker());

        optionAboutMe.setOnClickListener(v -> {
            // Launch EditUserProfileActivity as a pop-up dialog style
            Intent intent = new Intent(Profile.this, EditUserProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImageToCloudinary(Uri imageUri) {
        if (user == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .unsigned("tail_wagging") // Use your unsigned preset
                .option("folder", "profile_pics/")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveProfileImageUrl(imageUrl, progressDialog);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(Profile.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void saveProfileImageUrl(String imageUrl, ProgressDialog progressDialog) {
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }
        dbRef.child("users").child(user.getUid()).child("photoUrl")
                .setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Glide.with(Profile.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(profileImage);
                    Toast.makeText(Profile.this, "Profile image updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}