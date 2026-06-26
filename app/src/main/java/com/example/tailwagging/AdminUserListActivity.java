package com.example.tailwagging;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private ProgressBar pbUsers;
    private AdminUserAdapter adapter;
    private final List<AdminUser> userList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        rvUsers = findViewById(R.id.rvAdminUsers);
        pbUsers = findViewById(R.id.pbAdminUsers);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(userList);
        rvUsers.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        fetchUsers();
    }

    private void fetchUsers() {
        pbUsers.setVisibility(View.VISIBLE);
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AdminUser user = ds.getValue(AdminUser.class);
                    if (user != null) {
                        user.id = ds.getKey();
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                pbUsers.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbUsers.setVisibility(View.GONE);
                Toast.makeText(AdminUserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
