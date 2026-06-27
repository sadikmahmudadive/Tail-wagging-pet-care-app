package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView rvUsers;
    private ProgressBar pbUsers;
    private AdminUserAdapter adapter;
    private final List<AdminUser> userList = new ArrayList<>();
    private final List<AdminUser> allUsers = new ArrayList<>();
    private DatabaseReference dbRef;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminUserListRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvUsers = findViewById(R.id.rvAdminUsers);
        pbUsers = findViewById(R.id.pbAdminUsers);
        etSearch = findViewById(R.id.etSearchUsers);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(userList, this);
        rvUsers.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        fetchUsers();
        setupSearch();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(allUsers);
        } else {
            for (AdminUser u : allUsers) {
                if ((u.name != null && u.name.toLowerCase().contains(query.toLowerCase())) ||
                    (u.email != null && u.email.toLowerCase().contains(query.toLowerCase()))) {
                    userList.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchUsers() {
        pbUsers.setVisibility(View.VISIBLE);
        dbRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AdminUser user = ds.getValue(AdminUser.class);
                    if (user != null) {
                        user.id = ds.getKey();
                        allUsers.add(user);
                    }
                }
                filterUsers(etSearch.getText().toString());
                pbUsers.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbUsers.setVisibility(View.GONE);
                Toast.makeText(AdminUserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(AdminUser user) {
        if ("admin@mail.com".equalsIgnoreCase(user.email)) {
            Toast.makeText(this, "Cannot delete main admin", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.name + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbRef.child("users").child(user.id).removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleVerify(AdminUser user) {
        boolean nextState = !user.isVerified;
        dbRef.child("users").child(user.id).child("isVerified").setValue(nextState)
                .addOnSuccessListener(aVoid -> {
                    String msg = nextState ? "User verified" : "Verification removed";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onManageShop(AdminUser user) {
        Intent intent = new Intent(this, InventoryManagementActivity.class);
        intent.putExtra("SHOP_ID", user.id);
        intent.putExtra("SHOP_NAME", user.name);
        startActivity(intent);
    }
}
