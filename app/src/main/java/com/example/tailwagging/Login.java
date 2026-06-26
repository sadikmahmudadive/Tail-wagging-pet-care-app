package com.example.tailwagging;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "Login";
    TextView btnSignup, btnForgetPassword;
    EditText textEmail, textPassword;
    Button btnLogin, btnGoogle, btnFacebook;
    FirebaseAuth authLogin;
    GoogleSignInClient googleSignInClient;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash:", keyHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnLogin = findViewById(R.id.btn_Login);
        btnSignup = findViewById(R.id.btn_Signup);
        btnForgetPassword = findViewById(R.id.btn_forget_password);
        textEmail = findViewById(R.id.text_email);
        textPassword = findViewById(R.id.text_password);
        btnGoogle = findViewById(R.id.btn_google);

        authLogin = FirebaseAuth.getInstance();

        // Use your custom Firebase Realtime Database URL
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        // Google Sign-In Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (authLogin.getCurrentUser() != null) {
            checkUserRoleAndRedirect(authLogin.getCurrentUser().getUid());
        }

        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registration.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String email = textEmail.getText().toString().trim();
            String password = textPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                authLogin.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authLogin.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndRedirect(user.getUid());
                        }
                    } else {
                        Toast.makeText(Login.this, "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error during login", e);
                Toast.makeText(Login.this, "An error occurred during login", Toast.LENGTH_SHORT).show();
            }
        });

        btnForgetPassword.setOnClickListener(v -> {
            String email = textEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter your registered email", Toast.LENGTH_SHORT).show();
                return;
            }

            authLogin.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Password reset email sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Login.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void checkUserRoleAndRedirect(String uid) {
        FirebaseUser user = authLogin.getCurrentUser();
        if (user != null && "admin@mail.com".equalsIgnoreCase(user.getEmail())) {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", "Pet Owner").apply();
            startActivity(new Intent(Login.this, AdminDashboardActivity.class));
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
                        startActivity(new Intent(Login.this, VetDashboardActivity.class));
                    } else {
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }
                    finish();
                } else {
                    // Default to Pet Owner if no role found (for legacy users)
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Login.this, "Error checking user role", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: resultCode = " + resultCode);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed: Status Code = " + e.getStatusCode(), e);
                String errorMessage = "Google sign-in failed. Error Code: " + e.getStatusCode();
                if (e.getStatusCode() == 10) {
                    errorMessage += " (Developer Error - Check SHA-1)";
                } else if (e.getStatusCode() == 12500) {
                    errorMessage += " (Sign-In Failed)";
                }
                Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authLogin.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authLogin.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // New user, save basic data
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("name", user.getDisplayName());
                                        userData.put("email", user.getEmail());
                                        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "default_url");
                                        userData.put("role", "Pet Owner"); // Default role
                                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", "Pet Owner").commit();

                                        dbRef.child("users").child(uid).setValue(userData).addOnSuccessListener(aVoid -> {
                                            updateFcmToken(uid);
                                            startActivity(new Intent(Login.this, MainActivity.class));
                                            finish();
                                        });
                                    } else {
                                        // Existing user, just redirect
                                        checkUserRoleAndRedirect(uid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    checkUserRoleAndRedirect(uid);
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Firebase Auth Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        Toast.makeText(Login.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}