package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditUserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private ImageButton btnUploadPhoto;
    private Button btnSaveProfile;
    private EditText editEmail, editUsername, editPassword, editPhone, editAddress;
    private ImageView backButton, menuButton;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private boolean isEditable = false;
    private static final int PICK_IMAGE = 1;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dialog theme before super.onCreate
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
        db = FirebaseFirestore.getInstance();

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
        // Email from Firebase Auth
        editEmail.setText(user.getEmail());

        // Username and other fields from Firestore
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editUsername.setText(documentSnapshot.getString("name"));
                editPhone.setText(documentSnapshot.getString("phone"));
                editAddress.setText(documentSnapshot.getString("address"));

                // Load profile image if stored (example: store image URL in "profileImageUrl")
                String imgUrl = documentSnapshot.getString("profileImageUrl");
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_profile).into(profileImage);
                }
            }
        });
    }

    private void saveProfileData() {
        if (user == null) return;
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Update Firestore
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.update(
                "name", username,
                "phone", phone,
                "address", address
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Optionally update password if changed (Firebase Authentication)
        if (!password.isEmpty()) {
            user.updatePassword(password)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // Optionally upload the profile image if changed (left for implementation)
        // You'd typically upload to Firebase Storage and update the profileImageUrl in Firestore
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
                    .into(profileImage);
            // You can upload the imageUri to Firebase Storage here and update Firestore with the URL
        }
    }
}