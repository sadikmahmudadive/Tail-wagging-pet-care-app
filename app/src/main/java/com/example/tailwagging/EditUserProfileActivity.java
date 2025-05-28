package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditUserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private ImageButton btnUploadPhoto;
    private Button btnSaveProfile;
    private EditText editEmail, editUsername, editPassword, editPhone, editAddress;
    private ImageView backButton, menuButton;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;
    private StorageReference storageReference;

    private boolean isEditable = false;
    private static final int PICK_IMAGE = 1;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MyCustomDialogTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // Set custom width and height for dialog-style activity
        WindowManager.LayoutParams params = getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        params.width = (int) (dm.widthPixels * 0.95);   // 95% of screen width
        params.height = (int) (dm.heightPixels * 0.95); // 95% of screen height
        getWindow().setAttributes(params);

        // Views
        profileImage = findViewById(R.id.profileImage);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Menu button (3-dot)
        menuButton.setOnClickListener(v -> showEditPopup());

        // Upload photo
        btnUploadPhoto.setOnClickListener(v -> {
            if (isEditable) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        // Save profile
        btnSaveProfile.setOnClickListener(v -> {
            if (isEditable) saveProfileData();
        });

        // Initially load user data and disable fields
        loadUserData();
        setEditable(false);
    }

    private void showEditPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setMessage("Do you want to edit your profile?")
                .setPositiveButton("Yes", (dialog, which) -> setEditable(true))
                .setNegativeButton("No", (dialog, which) -> setEditable(false))
                .show();
    }

    private void setEditable(boolean editable) {
        isEditable = editable;
        editEmail.setEnabled(false); // Email typically not editable
        editUsername.setEnabled(editable);
        editPassword.setEnabled(editable);
        editPhone.setEnabled(editable);
        editAddress.setEnabled(editable);
        btnUploadPhoto.setEnabled(editable);
        btnSaveProfile.setEnabled(editable);
    }

    private void loadUserData() {
        if (user == null) return;
        editEmail.setText(user.getEmail());

        dbRef.child("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            editUsername.setText(snapshot.child("name").getValue(String.class));
                            editPhone.setText(snapshot.child("phone").getValue(String.class));
                            editAddress.setText(snapshot.child("address").getValue(String.class));

                            String imgUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            Log.d("PROFILE_IMG", "profileImageUrl from RTDB: " + imgUrl);
                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                Glide.with(EditUserProfileActivity.this)
                                        .load(imgUrl)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(EditUserProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveProfileData() {
        if (user == null) return;
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", username);
        updateData.put("phone", phone);
        updateData.put("address", address);

        if (imageUri != null) {
            StorageReference imgRef = storageReference.child("users").child(user.getUid()).child("profile.jpg");
            imgRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateData.put("profileImageUrl", uri.toString());
                        dbRef.child("users").child(user.getUid()).updateChildren(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                                    loadUserData();
                                    setEditable(false);
                                    editPassword.setText("");
                                    imageUri = null;
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            dbRef.child("users").child(user.getUid()).updateChildren(updateData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        loadUserData();
                        setEditable(false);
                        editPassword.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // Optionally update password if changed (Firebase Authentication)
        if (!password.isEmpty()) {
            user.updatePassword(password)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Use Glide to show the selected image
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
            // Image will be uploaded on save
        }
    }
}