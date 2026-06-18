package com.example.tailwagging;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.util.Map;

public class Profile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userEmail, userPhoneNumber, userAddress;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        profileImage = findViewById(R.id.profileImage);
        userEmail = findViewById(R.id.userEmail);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        userAddress = findViewById(R.id.userAddress);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutProfile);

        NavbarHelper.setupNavbar(this);

        // Bind the action button if it exists (Provider FAB)
        View navAdd = findViewById(R.id.navProviderAdd);
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> {
                startActivity(new Intent(Profile.this, VetDashboardActivity.class));
            });
        }

        reloadUserData();

        profileImage.setOnClickListener(v -> openImagePicker());

        findViewById(R.id.optionAboutMe).setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditUserProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aboutMeLauncher.launch(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::reloadUserData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadUserData();
    }

    private void reloadUserData() {
        if (user == null) {
            userEmail.setText("No user");
            userPhoneNumber.setText("No phone number");
            profileImage.setImageResource(R.drawable.ic_profile);
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
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
                            String name = dataSnapshot.child("name").getValue(String.class);
                            String address = dataSnapshot.child("address").getValue(String.class);
                            String role = dataSnapshot.child("role").getValue(String.class);
                            if (role != null) {
                                String cachedRole = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_role", "Pet Owner");
                                if (!role.trim().equalsIgnoreCase(cachedRole.trim())) {
                                    getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role.trim()).apply();
                                    NavbarHelper.setupNavbar(Profile.this);
                                    // Re-bind action button after re-inflation
                                    View newNavAdd = findViewById(R.id.navProviderAdd);
                                    if (newNavAdd != null) {
                                        newNavAdd.setOnClickListener(v2 -> {
                                            startActivity(new Intent(Profile.this, VetDashboardActivity.class));
                                        });
                                    }
                                }
                            }

                            if (name != null) {
                                ((TextView)findViewById(R.id.userName)).setText(name);
                                ((TextView)findViewById(R.id.userNameHeader)).setText(name);
                            }

                            if (phone != null && !phone.isEmpty()) {
                                userPhoneNumber.setText(phone);
                            } else {
                                userPhoneNumber.setText("No phone number");
                            }

                            if (address != null && !address.isEmpty()) {
                                userAddress.setText(address);
                            } else {
                                userAddress.setText("No address added");
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
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Profile.this, "Failed to load profile: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
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
                .unsigned("tail_wagging")
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
                    reloadUserData();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}