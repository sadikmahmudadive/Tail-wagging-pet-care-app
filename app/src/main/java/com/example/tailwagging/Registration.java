package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    EditText textEmail, textPassword, textName;
    TextView btnLogin;
    Button btnSignup;
    RadioButton radioPetOwner, radioVet, radioGroomer, radioBoarding;

    FirebaseAuth auth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textEmail = findViewById(R.id.text_emailR);
        textPassword = findViewById(R.id.text_passwordR);
        textName = findViewById(R.id.text_nameR);
        btnLogin = findViewById(R.id.btn_LoginR);
        btnSignup = findViewById(R.id.btn_SignupR);
        
        radioPetOwner = findViewById(R.id.radioPetOwner);
        radioVet = findViewById(R.id.radioVet);
        radioGroomer = findViewById(R.id.radioGroomer);
        radioBoarding = findViewById(R.id.radioBoarding);

        setupRadioButtons();

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        if (auth.getCurrentUser() != null) {
            checkUserRoleAndRedirect(auth.getCurrentUser().getUid());
        }

        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
            finish();
        });

        btnSignup.setOnClickListener(view -> {
            String email = textEmail.getText().toString().trim();
            String password = textPassword.getText().toString().trim();
            String name = textName.getText().toString().trim();
            
            final String role = getSelectedRole();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Registration.this, "Email Field is Empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Registration.this, "Password Field is Empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Registration.this, "Password must be more than 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("role", role);
                                userData.put("photoUrl", "");

                                dbRef.child("users").child(userId)
                                        .setValue(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Registration.this, "User data added successfully", Toast.LENGTH_SHORT).show();
                                            checkUserRoleAndRedirect(userId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Registration.this, "Error adding data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(Registration.this, "User not Created", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void setupRadioButtons() {
        radioPetOwner.setOnClickListener(v -> handleRadioSelection(radioPetOwner));
        radioVet.setOnClickListener(v -> handleRadioSelection(radioVet));
        radioGroomer.setOnClickListener(v -> handleRadioSelection(radioGroomer));
        radioBoarding.setOnClickListener(v -> handleRadioSelection(radioBoarding));
    }

    private void updateFcmToken(String uid) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        dbRef.child("users").child(uid).child("fcmToken").setValue(token);
                    }
                });
    }

    private void handleRadioSelection(RadioButton selected) {
        radioPetOwner.setChecked(false);
        radioVet.setChecked(false);
        radioGroomer.setChecked(false);
        radioBoarding.setChecked(false);
        selected.setChecked(true);
    }

    private String getSelectedRole() {
        if (radioVet.isChecked()) return "Veterinarian";
        if (radioGroomer.isChecked()) return "Grooming";
        if (radioBoarding.isChecked()) return "Boarding";
        return "Pet Owner";
    }

    private void checkUserRoleAndRedirect(String uid) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && "admin@mail.com".equalsIgnoreCase(user.getEmail())) {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", "Pet Owner").apply();
            startActivity(new Intent(Registration.this, AdminDashboardActivity.class));
            finish();
            return;
        }

        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if (role != null) {
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role.trim()).commit();
                        updateFcmToken(uid);
                    }
                    if ("Veterinarian".equalsIgnoreCase(role) || "Grooming".equalsIgnoreCase(role) || "Boarding".equalsIgnoreCase(role)) {
                        startActivity(new Intent(Registration.this, VetDashboardActivity.class));
                    } else {
                        startActivity(new Intent(Registration.this, MainActivity.class));
                    }
                    finish();
                } else {
                    startActivity(new Intent(Registration.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                startActivity(new Intent(Registration.this, Login.class));
                finish();
            }
        });
    }
}