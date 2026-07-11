package com.example.tailwagging;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private Order order;
    private DatabaseReference dbRef;
    private String currentUserId;
    private boolean isManagementMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);

        order = (Order) getIntent().getSerializableExtra("SELECTED_ORDER");
        if (order == null) {
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();
        
        checkManagementPrivileges();
        initWidgets();
        displayOrderDetails();
    }

    private void checkManagementPrivileges() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if ("admin@mail.com".equalsIgnoreCase(email)) {
            isManagementMode = true;
            return;
        }

        // If not admin, check if current user is a shop owner for any item in this order
        if (order.items != null) {
            for (CartItem item : order.items) {
                if (currentUserId != null && currentUserId.equals(item.shopId)) {
                    isManagementMode = true;
                    break;
                }
            }
        }
    }

    private void initWidgets() {
        View root = findViewById(android.R.id.content);
        UiUtils.fadeIn(root);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsAppBar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvOrderHeaderId)).setText("Order #" + order.orderId.substring(Math.max(0, order.orderId.length() - 8)));

        if (isManagementMode) {
            findViewById(R.id.layoutActionButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.btnUpdateStatus).setOnClickListener(v -> showStatusUpdateDialog());
            findViewById(R.id.btnCancelOrder).setOnClickListener(v -> cancelOrder());
        }
    }

    private void displayOrderDetails() {
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        ImageView ivStatus = findViewById(R.id.ivStatusIcon);
        
        String status = order.status != null ? order.status : "Pending";
        tvStatus.setText(status);
        
        // Dynamic status colors
        if ("Delivered".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(getColor(R.color.health_green));
            ivStatus.setColorFilter(getColor(R.color.health_green));
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(getColor(R.color.red));
            ivStatus.setColorFilter(getColor(R.color.red));
        }

        updateTrackingTimeline(status);

        ((TextView) findViewById(R.id.tvDetailCustomerName)).setText(order.userName != null ? order.userName : "Guest");
        ((TextView) findViewById(R.id.tvDetailCustomerPhone)).setText(order.phone != null ? order.phone : "No phone provided");
        ((TextView) findViewById(R.id.tvDetailCustomerAddress)).setText(order.address != null ? order.address : "No address provided");
        ((TextView) findViewById(R.id.tvDetailPaymentMethod)).setText(order.paymentMethod);
        ((TextView) findViewById(R.id.tvDetailTotal)).setText(String.format(Locale.getDefault(), "Tk %.2f", order.total));

        RecyclerView rvItems = findViewById(R.id.rvOrderItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        
        // Re-using CartAdapter but in "read-only" mode (passing null for listener)
        CartAdapter adapter = new CartAdapter(order.items, null);
        rvItems.setAdapter(adapter);
    }

    private void updateTrackingTimeline(String status) {
        View v1 = findViewById(R.id.viewStep1);
        View v2 = findViewById(R.id.viewStep2);
        View v3 = findViewById(R.id.viewStep3);
        View v4 = findViewById(R.id.viewStep4);
        
        View l1 = findViewById(R.id.line1);
        View l2 = findViewById(R.id.line2);
        View l3 = findViewById(R.id.line3);
        
        TextView t1 = findViewById(R.id.tvStep1Label);
        TextView t2 = findViewById(R.id.tvStep2Label);
        TextView t3 = findViewById(R.id.tvStep3Label);
        TextView t4 = findViewById(R.id.tvStep4Label);

        int colorGreen = getColor(R.color.health_green);
        int colorGray = getColor(R.color.light_gray);
        
        // Reset all to gray/inactive
        v2.setBackgroundResource(R.drawable.bg_circle_gray);
        v3.setBackgroundResource(R.drawable.bg_circle_gray);
        v4.setBackgroundResource(R.drawable.bg_circle_gray);
        l1.setBackgroundColor(colorGray);
        l2.setBackgroundColor(colorGray);
        l3.setBackgroundColor(colorGray);
        t2.setTypeface(null, android.graphics.Typeface.NORMAL);
        t3.setTypeface(null, android.graphics.Typeface.NORMAL);
        t4.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Always step 1 is green
        l1.setBackgroundColor(colorGreen);

        if ("Processing".equalsIgnoreCase(status) || "Shipped".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status)) {
            v2.setBackgroundResource(R.drawable.bg_circle_green);
            l2.setBackgroundColor(colorGreen);
            t2.setTextColor(colorGreen);
            t2.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if ("Shipped".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status)) {
            v3.setBackgroundResource(R.drawable.bg_circle_green);
            l3.setBackgroundColor(colorGreen);
            t3.setTextColor(colorGreen);
            t3.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if ("Delivered".equalsIgnoreCase(status)) {
            v4.setBackgroundResource(R.drawable.bg_circle_green);
            t4.setTextColor(colorGreen);
            t4.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if ("Cancelled".equalsIgnoreCase(status)) {
            int colorRed = getColor(R.color.red);
            t1.setText("Order Cancelled");
            t1.setTextColor(colorRed);
            v1.setBackgroundResource(R.drawable.bg_circle_gray);
            l1.setVisibility(View.GONE);
            // Just simple text change for now
        }
    }

    private void showStatusUpdateDialog() {
        String[] statuses = {"Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
        new AlertDialog.Builder(this)
                .setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> updateOrderStatus(statuses[which]))
                .show();
    }

    private void updateOrderStatus(String newStatus) {
        dbRef.child("orders").child(order.orderId).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    order.status = newStatus;
                    displayOrderDetails();
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> updateOrderStatus("Cancelled"))
                .setNegativeButton("No", null)
                .show();
    }
}
