package com.example.tailwagging;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddEditPet extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int REQUEST_FIND_PET_TYPE = 201;

    private CircleImageView petImageInput;
    private EditText etPetName, etBreedName, etPetAge, etPetDob, etPetColor, etPetSound, etPetHeight, etPetWeight, etPetDescription;
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
    private Pet petToEdit;

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
        etPetDescription = findViewById(R.id.etPetDescription);
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

        // Check if editing
        petToEdit = getIntent().getParcelableExtra("EDIT_PET");
        if (petToEdit != null) {
            populateFieldsForEdit();
            if (btnAddPet instanceof Button) {
                ((Button) btnAddPet).setText("Update Pet");
            }
        }

        backBtn.setOnClickListener(v -> {
            finish();
        });

        // When a user selects a photo, pass it automatically to FindPetTypeActivity
        btnFindBreedType.setOnClickListener(v -> {
            if (imageUri == null && (petToEdit == null || petToEdit.getImageUrl() == null)) {
                Toast.makeText(this, "Please select a pet photo first.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, FindPetTypeActivity.class);
            if (imageUri != null) {
                intent.putExtra("PET_IMAGE_URI", imageUri.toString());
            } else if (petToEdit != null) {
                intent.putExtra("PET_IMAGE_URL", petToEdit.getImageUrl());
            }
            startActivityForResult(intent, REQUEST_FIND_PET_TYPE);
        });

        btnUploadPhoto.setOnClickListener(v -> openImagePicker());
        petImageInput.setOnClickListener(v -> openImagePicker());
        btnAddPet.setOnClickListener(v -> validateAndProcessPet());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearForm();
            showPets();
            swipeRefreshLayout.setRefreshing(false);
        });

        showPets();
        NavbarHelper.setupNavbar(this);
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

            // Automatically launch FindPetTypeActivity after photo selection
            Intent intent = new Intent(this, FindPetTypeActivity.class);
            intent.putExtra("PET_IMAGE_URI", imageUri.toString());
            startActivityForResult(intent, REQUEST_FIND_PET_TYPE);
        }
        // Handle result from FindPetTypeActivity
        if (requestCode == REQUEST_FIND_PET_TYPE && resultCode == RESULT_OK && data != null) {
            String predictedSpecies = data.getStringExtra("PREDICTED_SPECIES");
            if (predictedSpecies != null && !predictedSpecies.isEmpty()) {
                etBreedName.setText(predictedSpecies);
                Toast.makeText(this, "Predicted species: " + predictedSpecies, Toast.LENGTH_SHORT).show();
            }
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
                    String dob = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
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

    private void populateFieldsForEdit() {
        etPetName.setText(petToEdit.getName());
        etBreedName.setText(petToEdit.getBreed());
        etPetGender.setText(petToEdit.getGender());
        etPetAge.setText(petToEdit.getAge());
        etPetDob.setText(petToEdit.getDob());
        etPetColor.setText(petToEdit.getColor());
        etPetSound.setText(petToEdit.getSound());
        etPetHeight.setText(petToEdit.getHeight());
        etPetWeight.setText(petToEdit.getWeight());
        etPetDescription.setText(petToEdit.getDescription());
        if (petToEdit.getImageUrl() != null && !petToEdit.getImageUrl().isEmpty()) {
            Glide.with(this).load(petToEdit.getImageUrl()).placeholder(R.drawable.ic_profile).into(petImageInput);
        }
    }

    private void validateAndProcessPet() {
        String name = etPetName.getText().toString().trim();
        if (name.isEmpty()) { etPetName.setError("Name required"); etPetName.requestFocus(); return; }
        
        if (imageUri == null && (petToEdit == null || petToEdit.getImageUrl() == null)) { 
            Toast.makeText(this, "Please select pet photo", Toast.LENGTH_SHORT).show(); 
            return; 
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(petToEdit == null ? "Adding Pet..." : "Updating Pet...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uid = (user != null) ? user.getUid() : null;
        if (uid == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // Upload new image to Cloudinary
            MediaManager.get().upload(imageUri)
                    .unsigned("tail_wagging")
                    .option("folder", "pets/")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            saveOrUpdatePetDetails(imageUrl, progressDialog, uid);
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
        } else {
            // Use existing image URL
            saveOrUpdatePetDetails(petToEdit.getImageUrl(), progressDialog, uid);
        }
    }

    private void saveOrUpdatePetDetails(String imageUrl, ProgressDialog progressDialog, String uid) {
        DatabaseReference petsRef = dbRef.child("pets");
        String petId;
        
        if (petToEdit != null) {
            petId = petToEdit.getPetID();
        } else {
            petId = petsRef.push().getKey();
        }

        if (petId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to generate/retrieve pet ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> petMap = new HashMap<>();
        petMap.put("petID", petId);
        petMap.put("ownerID", uid);
        petMap.put("name", etPetName.getText().toString().trim());
        petMap.put("breed", etBreedName.getText().toString().trim());
        petMap.put("gender", etPetGender.getText().toString().trim());
        petMap.put("age", etPetAge.getText().toString().trim());
        petMap.put("dob", etPetDob.getText().toString().trim());
        petMap.put("color", etPetColor.getText().toString().trim());
        petMap.put("sound", etPetSound.getText().toString().trim());
        petMap.put("height", etPetHeight.getText().toString().trim());
        petMap.put("weight", etPetWeight.getText().toString().trim());
        petMap.put("description", etPetDescription.getText().toString().trim());
        petMap.put("imageUrl", imageUrl);

        petsRef.child(petId)
                .setValue(petMap)
                .addOnSuccessListener(aVoid -> {
                    // Automatically add birthday to local events
                    addBirthdayToEvents(petId, etPetName.getText().toString(), etPetDob.getText().toString());

                    progressDialog.dismiss();
                    String msg = (petToEdit == null) ? "Pet added!" : "Pet updated!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    if (petToEdit != null) {
                        finish(); // Go back to details if editing
                    } else {
                        clearForm();
                        showPets(); // Refresh list if adding
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addBirthdayToEvents(String petId, String petName, String dob) {
        if (TextUtils.isEmpty(dob)) return;
        
        try {
            String[] parts = dob.split("-");
            if (parts.length == 3) {
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                int currentYear = LocalDate.now().getYear();
                
                // Create event for current year
                LocalDate bdayDate = LocalDate.of(currentYear, month, day);
                
                int eventId = ("birthday_" + petId).hashCode();
                String currentUserId = FirebaseAuth.getInstance().getUid();
                Event bdayEvent = new Event(
                        eventId,
                        currentUserId,
                        petName + "'s Birthday",
                        "Birthday",
                        "Happy Birthday to " + petName + "!",
                        petName,
                        petId,
                        bdayDate,
                        LocalTime.MIDNIGHT,
                        LocalTime.MAX,
                        true
                );
                
                EventStore eventStore = EventStore.getInstance(this);
                // Remove old one if exists (e.g. if dob changed)
                eventStore.removeEvent(eventId);
                eventStore.addEvent(bdayEvent);
                AlarmHelper.setEventAlarm(this, bdayEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        etPetName.setText(""); etBreedName.setText(""); etPetGender.setText(""); etPetAge.setText("");
        etPetDob.setText(""); etPetColor.setText(""); etPetSound.setText(""); etPetHeight.setText(""); 
        etPetWeight.setText(""); etPetDescription.setText("");
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

                            LinearLayout petItem = new LinearLayout(AddEditPet.this);
                            petItem.setOrientation(LinearLayout.VERTICAL);
                            petItem.setGravity(android.view.Gravity.CENTER);
                            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                                    200, LinearLayout.LayoutParams.WRAP_CONTENT);
                            itemParams.setMargins(0, 0, 32, 0);
                            petItem.setLayoutParams(itemParams);

                            CircleImageView img = new CircleImageView(AddEditPet.this);
                            img.setLayoutParams(new LinearLayout.LayoutParams(140, 140));
                            img.setPadding(4, 4, 4, 4);
                            img.setBorderWidth(4);
                            img.setBorderColor(getResources().getColor(R.color.md_theme_light_primaryContainer));
                            Glide.with(AddEditPet.this).load(imageUrl).placeholder(R.drawable.ic_profile).into(img);

                            TextView tv = new TextView(AddEditPet.this);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tv.setText(name);
                            tv.setTextSize(12);
                            tv.setMaxLines(1);
                            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                            tv.setTextColor(getResources().getColor(R.color.black));
                            tv.setPadding(0, 8, 0, 0);

                            petItem.addView(img);
                            petItem.addView(tv);

                            layoutAddedPets.addView(petItem);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddEditPet.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}