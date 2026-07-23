package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView rvFeed;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutEmpty;
    private EditText etSearch;
    private CommunityFeedAdapter adapter;
    private final List<FeedPost> allPosts = new ArrayList<>();
    private final List<FeedPost> filteredPosts = new ArrayList<>();
    private DatabaseReference dbRef;
    private String currentFilter = "All";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_community);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("community_posts");

        rvFeed = findViewById(R.id.rvCommunityFeed);
        swipeRefresh = findViewById(R.id.swipeRefreshCommunity);
        layoutEmpty = findViewById(R.id.layoutEmptyCommunity);
        etSearch = findViewById(R.id.etCommunitySearch);
        ChipGroup cgFilters = findViewById(R.id.cgPostFilters);

        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommunityFeedAdapter(filteredPosts);
        rvFeed.setAdapter(adapter);

        findViewById(R.id.fabCreatePost).setOnClickListener(v -> {
            UiUtils.animateClick(v);
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentFilter = "All";
            else if (id == R.id.chipAdoption) currentFilter = "ADOPTION";
            else if (id == R.id.chipRescue) currentFilter = "RESCUE";
            else if (id == R.id.chipMoments) currentFilter = "MOMENT";
            
            applyFilterAndSearch();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        swipeRefresh.setOnRefreshListener(this::fetchPosts);
        
        fetchPosts();
        NavbarHelper.setupNavbar(this);
    }

    private void fetchPosts() {
        swipeRefresh.setRefreshing(true);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPosts.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FeedPost post = ds.getValue(FeedPost.class);
                    if (post != null) allPosts.add(post);
                }
                allPosts.sort((p1, p2) -> Long.compare(p2.timestamp, p1.timestamp)); // Newest first
                applyFilterAndSearch();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(CommunityActivity.this, "Failed to load feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilterAndSearch() {
        filteredPosts.clear();
        for (FeedPost post : allPosts) {
            boolean matchesFilter = currentFilter.equals("All") || currentFilter.equals(post.postType);
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (post.content != null && post.content.toLowerCase().contains(searchQuery)) ||
                    (post.userName != null && post.userName.toLowerCase().contains(searchQuery));

            if (matchesFilter && matchesSearch) {
                filteredPosts.add(post);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredPosts.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFeed.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFeed.setVisibility(View.VISIBLE);
        }
    }
}
