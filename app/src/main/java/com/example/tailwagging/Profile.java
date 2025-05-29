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

    // Launcher for picking image
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Glide.with(this).load(imageUri).into(profileImage);
                        uploadProfileImageToCloudinary(imageUri);
                    }
                }
            }
    );

    // Launcher for editing About Me
    private final ActivityResultLauncher<Intent> aboutMeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> reloadUserData()
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

        LinearLayout optionAboutMe = findViewById(R.id.optionAboutMe);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        profileImage = findViewById(R.id.profileImage);
        userEmail = findViewById(R.id.userEmail);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);

        reloadUserData();

        profileImage.setOnClickListener(v -> openImagePicker());

        optionAboutMe.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditUserProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aboutMeLauncher.launch(intent);
        });
    }

    private void reloadUserData() {
        if (user == null) {
            userEmail.setText("No user");
            userPhoneNumber.setText("No phone number");
            profileImage.setImageResource(R.drawable.ic_profile);
            return;
        }
        userEmail.setText(user.getEmail());
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
                .option("folder", "profile_pics/") // Optional: organize in folder
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
                    reloadUserData();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}