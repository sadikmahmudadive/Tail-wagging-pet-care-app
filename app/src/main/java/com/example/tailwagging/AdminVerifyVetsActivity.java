package com.example.tailwagging;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class AdminVerifyVetsActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView rvVets;
    private ProgressBar pbVets;
    private AdminUserAdapter adapter;
    private final List<AdminUser> vetList = new ArrayList<>();
    private final List<AdminUser> allVets = new ArrayList<>();
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

        rvVets = findViewById(R.id.rvAdminUsers);
        pbVets = findViewById(R.id.pbAdminUsers);
        etSearch = findViewById(R.id.etSearchUsers);
        ((TextView)findViewById(R.id.tvAdminUserListTitle)).setText("Vet Verification");
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvVets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(vetList, this);
        rvVets.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        fetchVets();
        setupSearch();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVets(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterVets(String query) {
        vetList.clear();
        if (query.isEmpty()) {
            vetList.addAll(allVets);
        } else {
            for (AdminUser u : allVets) {
                if ((u.name != null && u.name.toLowerCase().contains(query.toLowerCase())) ||
                    (u.email != null && u.email.toLowerCase().contains(query.toLowerCase()))) {
                    vetList.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchVets() {
        pbVets.setVisibility(View.VISIBLE);
        dbRef.child("users").orderByChild("role").equalTo("Veterinarian")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allVets.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AdminUser user = ds.getValue(AdminUser.class);
                    if (user != null) {
                        user.id = ds.getKey();
                        allVets.add(user);
                    }
                }
                filterVets(etSearch.getText().toString());
                pbVets.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbVets.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDelete(AdminUser user) {
        dbRef.child("users").child(user.id).removeValue();
    }

    @Override
    public void onToggleVerify(AdminUser user) {
        dbRef.child("users").child(user.id).child("isVerified").setValue(!user.isVerified);
    }
}
