package com.example.tailwagging;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the About Me option
        LinearLayout optionAboutMe = findViewById(R.id.optionAboutMe);

        optionAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch EditUserProfileActivity as a pop-up dialog style
                Intent intent = new Intent(Profile.this, EditUserProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // To show as a dialog, set a dialog theme in the manifest or programmatically
                startActivity(intent);
            }
        });
    }
}