package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnManageUsers).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminUserListActivity.class)));
            
        findViewById(R.id.btnManageVets).setOnClickListener(v -> 
            Toast.makeText(this, "Vet Verification coming soon", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.btnViewAnalytics).setOnClickListener(v -> 
            Toast.makeText(this, "Analytics coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnPushBroadcast).setOnClickListener(v -> 
            Toast.makeText(this, "Push Broadcast tool coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnUserView).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        findViewById(R.id.btnLogoutAdmin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
