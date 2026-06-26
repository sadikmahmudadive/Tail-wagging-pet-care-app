package com.example.tailwagging;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.format.DateTimeFormatter;

public class AdminBroadcastActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_broadcast);

        etTitle = findViewById(R.id.etBroadcastTitle);
        etMessage = findViewById(R.id.etBroadcastMessage);
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSendBroadcast).setOnClickListener(v -> sendBroadcast());
    }

    private void sendBroadcast() {
        String title = etTitle.getText().toString().trim();
        String msg = etMessage.getText().toString().trim();

        if (title.isEmpty() || msg.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
                
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (userId == null) continue;

                    String notifId = dbRef.child("notifications").child(userId).push().getKey();
                    NotificationItem item = new NotificationItem(notifId, "[Global] " + title, msg, timestamp, "SYSTEM");
                    
                    if (notifId != null) {
                        dbRef.child("notifications").child(userId).child(notifId).setValue(item);
                    }
                }
                Toast.makeText(AdminBroadcastActivity.this, "Broadcast sent to all users!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
