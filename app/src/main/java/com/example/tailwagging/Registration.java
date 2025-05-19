package com.example.tailwagging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Firestore import removed
//import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// Realtime Database import
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    EditText textEmail, textPassword, textName;
    TextView btnLogin;
    Button btnSignup, btnSelectPhoto;
    Uri image;
    ImageView profilePic;

    FirebaseAuth auth;
    // Firestore removed
    // FirebaseFirestore firestore;
    StorageReference storageReference;
    // Realtime Database reference
    DatabaseReference dbRef;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                image = result.getData().getData();
                btnSelectPhoto.setEnabled(true);
                Glide.with(getApplicationContext()).load(image).into(profilePic);
            }
        } else {
            Toast.makeText(Registration.this, "Please select an Image", Toast.LENGTH_SHORT).show();
        }
    });

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

        auth = FirebaseAuth.getInstance();
        // firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
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

                                dbRef.child("users").child(userId)
                                        .setValue(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Registration.this, "User data added successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Registration.this, Login.class));
                                            finish();
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
}