package com.example.tailwagging;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddEditPet extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private CircleImageView petImageInput;
    private EditText etPetName, etBreedName, etPetAge, etPetDob, etPetColor, etPetSound, etPetHeight, etPetWeight;
    private AutoCompleteTextView etPetGender;
    private ImageView backBtn;
    private Button btnFindBreedType;
    private View btnUploadPhoto, btnAddPet;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;
    private LinearLayout layoutAddedPets;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean cloudinaryInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        petImageInput = findViewById(R.id.petImageInput);
        etPetName = findViewById(R.id.etPetName);
        etBreedName = findViewById(R.id.etBreedName);
        etPetGender = findViewById(R.id.etPetGender);
        etPetAge = findViewById(R.id.etPetAge);
        etPetDob = findViewById(R.id.etPetDob);
        etPetColor = findViewById(R.id.etPetColor);
        etPetSound = findViewById(R.id.etPetSound);
        etPetHeight = findViewById(R.id.etPetHeight);
        etPetWeight = findViewById(R.id.etPetWeight);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnAddPet = findViewById(R.id.btnAddPet);
        layoutAddedPets = findViewById(R.id.layoutAddedPets);
        backBtn = findViewById(R.id.backBtn);
        btnFindBreedType = findViewById(R.id.btnFindBreedType);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        String[] genderOptions = new String[] { "Male", "Female", "Other" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        etPetGender.setAdapter(genderAdapter);
        etPetGender.setInputType(0);
        etPetGender.setKeyListener(null);
        etPetGender.setOnClickListener(v -> etPetGender.showDropDown());

        etPetDob.setOnClickListener(v -> showDatePickerDialog());

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AddEditPet.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnFindBreedType.setOnClickListener(v -> {
            Toast.makeText(this, "Find Breed feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnUploadPhoto.setOnClickListener(v -> openImagePicker());
        petImageInput.setOnClickListener(v -> openImagePicker());
        btnAddPet.setOnClickListener(v -> validateAndUploadPet());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearForm();
            showPets();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Initialize Cloudinary once
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dhm0edatk");
            config.put("api_key", "879315316647413");
            config.put("api_secret", "BgrjuKuPR_UqGZf2Gb5RHKDmF_0");
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }

        showPets();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(petImageInput);
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etPetDob.setText(dob);
                    setPetAgeFromDob(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setPetAgeFromDob(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        etPetAge.setText(age >= 0 ? String.valueOf(age) : "");
    }

    private void validateAndUploadPet() {
        String name = etPetName.getText().toString().trim();
        if (name.isEmpty()) { etPetName.setError("Name required"); etPetName.requestFocus(); return; }
        if (imageUri == null) { Toast.makeText(this, "Please select pet photo", Toast.LENGTH_SHORT).show(); return; }

        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            is.read();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot access selected image.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uid = (user != null) ? user.getUid() : null;
        if (uid == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image to Cloudinary
        MediaManager.get().upload(imageUri)
                .unsigned("tail_wagging") // Use unsigned preset, preferred for client side
                .option("folder", "pets/")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        savePetDetails(imageUrl, progressDialog, uid);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(AddEditPet.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void savePetDetails(String imageUrl, ProgressDialog progressDialog, String uid) {
        DatabaseReference petsRef = dbRef.child("pets");
        String petId = petsRef.push().getKey();
        if (petId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to generate pet ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> pet = new HashMap<>();
        pet.put("petID", petId);
        pet.put("ownerID", uid);
        pet.put("name", etPetName.getText().toString().trim());
        pet.put("breed", etBreedName.getText().toString().trim());
        pet.put("gender", etPetGender.getText().toString().trim());
        pet.put("age", etPetAge.getText().toString().trim());
        pet.put("dob", etPetDob.getText().toString().trim());
        pet.put("color", etPetColor.getText().toString().trim());
        pet.put("sound", etPetSound.getText().toString().trim());
        pet.put("height", etPetHeight.getText().toString().trim());
        pet.put("weight", etPetWeight.getText().toString().trim());
        pet.put("imageUrl", imageUrl);

        petsRef.child(petId)
                .setValue(pet)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Pet added!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    showPets(); // Refresh pets list
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etPetName.setText(""); etBreedName.setText(""); etPetGender.setText(""); etPetAge.setText("");
        etPetDob.setText(""); etPetColor.setText(""); etPetSound.setText(""); etPetHeight.setText(""); etPetWeight.setText("");
        petImageInput.setImageResource(R.drawable.ic_profile);
        imageUri = null;
    }

    private void showPets() {
        layoutAddedPets.removeAllViews();

        String uid = (user != null) ? user.getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference petsRef = dbRef.child("pets");
        petsRef.orderByChild("ownerID").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        layoutAddedPets.removeAllViews();
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(AddEditPet.this, "No pets found. Please add a pet.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (DataSnapshot petSnap : dataSnapshot.getChildren()) {
                            String name = petSnap.child("name").getValue(String.class);
                            String imageUrl = petSnap.child("imageUrl").getValue(String.class);

                            LinearLayout petLayout = new LinearLayout(AddEditPet.this);
                            petLayout.setOrientation(LinearLayout.HORIZONTAL);
                            petLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) getResources().getDimension(R.dimen.pet_item_height)
                            ));
                            petLayout.setBackgroundResource(R.drawable.bg_pet_card);
                            petLayout.setPadding(12, 8, 12, 8);

                            CircleImageView img = new CircleImageView(AddEditPet.this);
                            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(54,54);
                            imgParams.setMarginStart(12);
                            img.setLayoutParams(imgParams);
                            img.setBackgroundResource(R.drawable.bg_circle_accent);
                            Glide.with(AddEditPet.this).load(imageUrl).placeholder(R.drawable.ic_profile).into(img);

                            EditText tv = new EditText(AddEditPet.this);
                            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            tvParams.setMarginStart(20);
                            tv.setLayoutParams(tvParams);
                            tv.setText(name);
                            tv.setTextSize(18);
                            tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                            tv.setTextColor(getResources().getColor(R.color.mat_black));
                            tv.setEnabled(false);

                            petLayout.addView(img);
                            petLayout.addView(tv);

                            layoutAddedPets.addView(petLayout);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddEditPet.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}