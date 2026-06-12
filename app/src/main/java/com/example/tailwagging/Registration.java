package com.example.tailwagging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class Registration extends AppCompatActivity {

    EditText textEmail, textPassword, textName;
    TextView btnLogin;
    Button btnSignup;
    RadioGroup radioGroupRole;

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
        radioGroupRole = findViewById(R.id.radioGroupRole);

        auth = FirebaseAuth.getInstance();
        // Use your custom Firebase Realtime Database URL
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
            
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            String role = (selectedRoleId == R.id.radioVet) ? "Veterinarian" : "Pet Owner";

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
                                userData.put("photoUrl", ""); // Initialize with empty string or default URL

                                dbRef.child("users").child(userId)
                                        .setValue(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Registration.this, "User data added successfully", Toast.LENGTH_SHORT).show();
                                            // Redirect to Login to trigger role check, or just call checkUserRoleAndRedirect directly
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

    private void checkUserRoleAndRedirect(String uid) {
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if ("Veterinarian".equalsIgnoreCase(role)) {
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