package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    checkUserRoleAndRedirect(user.getUid());
                } else {
                    Intent intent = new Intent(SplashScreen.this, OnbordingScreen1.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    private void checkUserRoleAndRedirect(String uid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && "admin@mail.com".equalsIgnoreCase(user.getEmail())) {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", "Pet Owner").apply();
            startActivity(new Intent(SplashScreen.this, AdminDashboardActivity.class));
            finish();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if (role != null) {
                        // Sync role to cache for instant navbar loading
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("user_role", role.trim()).commit();
                    }

                    updateFcmToken(uid);
                    
                    boolean isProvider = "Veterinarian".equalsIgnoreCase(role) || 
                                         "Grooming".equalsIgnoreCase(role) || 
                                         "Boarding".equalsIgnoreCase(role);
                    
                    boolean isPetShop = "Pet Shop".equalsIgnoreCase(role);

                    if (isProvider) {
                        startActivity(new Intent(SplashScreen.this, VetDashboardActivity.class));
                    } else if (isPetShop) {
                        startActivity(new Intent(SplashScreen.this, PetShopDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    }
                    finish();
                } else {
                    updateFcmToken(uid);
                    // Default to Pet Owner if no role found
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // If checking role fails, go to login just to be safe
                startActivity(new Intent(SplashScreen.this, Login.class));
                finish();
            }
        });
    }

    private void updateFcmToken(String uid) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
                        dbRef.child("users").child(uid).child("fcmToken").setValue(token);
                    }
                });
    }
}