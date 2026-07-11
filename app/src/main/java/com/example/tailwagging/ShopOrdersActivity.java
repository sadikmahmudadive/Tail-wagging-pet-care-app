package com.example.tailwagging;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private ProgressBar pbOrders;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    private DatabaseReference dbRef;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_orders);

        shopId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        rvOrders = findViewById(R.id.rvShopOrders);
        pbOrders = findViewById(R.id.pbOrders);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        rvOrders.setAdapter(adapter);

        fetchOrders();
    }

    private void fetchOrders() {
        pbOrders.setVisibility(View.VISIBLE);
        dbRef.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;
                String userEmail = user.getEmail();
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order != null) {
                        if ("admin@mail.com".equalsIgnoreCase(userEmail)) {
                            orderList.add(order);
                        } else if (shopId != null && shopId.equals(order.userId)) {
                            orderList.add(order);
                        } else {
                            boolean belongsToShop = false;
                            if (order.items != null) {
                                for (CartItem item : order.items) {
                                    if (shopId != null && shopId.equals(item.shopId)) {
                                        belongsToShop = true;
                                        break;
                                    }
                                }
                            }
                            if (belongsToShop) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                orderList.sort((o1, o2) -> Long.compare(o2.timestamp, o1.timestamp)); // Latest first
                adapter.notifyDataSetChanged();
                pbOrders.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbOrders.setVisibility(View.GONE);
                Toast.makeText(ShopOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
