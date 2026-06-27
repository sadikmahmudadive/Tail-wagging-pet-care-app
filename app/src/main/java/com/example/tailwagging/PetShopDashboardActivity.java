package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PetShopDashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvShopName, tvTotalProducts, tvLowStock, tvRating;
    private CircleImageView ivProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference dbRef;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_shop_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.petShopRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initWidgets();
        shopId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        setDynamicGreeting();
        fetchShopData();

        swipeRefreshLayout.setOnRefreshListener(this::fetchShopData);

        findViewById(R.id.btn_LogoutShop).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        });

        findViewById(R.id.actionManageProducts).setOnClickListener(v -> 
            startActivity(new Intent(this, InventoryManagementActivity.class)));

        findViewById(R.id.actionViewOrders).setOnClickListener(v -> 
            Toast.makeText(this, "Order Tracking coming soon", Toast.LENGTH_SHORT).show());

        NavbarHelper.setupNavbar(this);
    }

    private void initWidgets() {
        tvGreeting = findViewById(R.id.tvShopGreeting);
        tvShopName = findViewById(R.id.tvWelcomeShop);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvRating = findViewById(R.id.tvShopRating);
        ivProfile = findViewById(R.id.shopProfilePhoto);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutShop);
    }

    private void setDynamicGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 5 && timeOfDay < 12) tvGreeting.setText(R.string.good_morning);
        else if (timeOfDay >= 12 && timeOfDay < 17) tvGreeting.setText(R.string.good_afternoon);
        else if (timeOfDay >= 17 && timeOfDay < 21) tvGreeting.setText(R.string.good_evening);
        else tvGreeting.setText(R.string.good_night);
    }

    private void fetchShopData() {
        if (shopId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        dbRef.child("users").child(shopId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                
                float ratingValue = 0;
                Object rVal = snapshot.child("rating").getValue();
                if (rVal != null) ratingValue = ((Number) rVal).floatValue();

                tvShopName.setText(name != null ? name : "Pet Shop Owner");
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", ratingValue));

                Glide.with(PetShopDashboardActivity.this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
                
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Real stats from Products
        dbRef.child("products").orderByChild("shopId").equalTo(shopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long total = snapshot.getChildrenCount();
                        int lowStock = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Product p = ds.getValue(Product.class);
                            if (p != null && p.stockQuantity <= 5) lowStock++;
                        }
                        tvTotalProducts.setText(String.valueOf(total));
                        tvLowStock.setText(String.valueOf(lowStock));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
