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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.CallbackManager;
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
import com.google.firebase.firestore.FirebaseFirestore;

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
    FirebaseFirestore firestore;
    GoogleSignInClient googleSignInClient;
    CallbackManager mCallbackManager;

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
                Log.d("KeyHash:", keyHash); // This will print the correct key hash
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
// btnFacebook is removed

        authLogin = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

// Google Sign-In Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

// Facebook SDK initialization and callback manager are removed

// Redirect to MainActivity if user is already logged in
        if (authLogin.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registration.class));
            finish();
        });

// Facebook login logic is removed

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
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("name", user.getDisplayName());
                            String profilePicUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "default_url";
                            intent.putExtra("profile_pic_url", profilePicUrl);
                            startActivity(intent);
                            finish();
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

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // mCallbackManager.onActivityResult(requestCode, resultCode, data); // Facebook callback removed

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(Login.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
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
                            // Save user data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", user.getDisplayName());
                            userData.put("email", user.getEmail());
                            userData.put("photoUrl", user.getPhotoUrl().toString()); // Save photo URL
                            firestore.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Login.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(Login.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}