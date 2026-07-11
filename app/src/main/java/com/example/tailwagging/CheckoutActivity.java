package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvAddress, tvSubtotal, tvTotal;
    private CartManager cartManager;
    private DatabaseReference dbRef;
    private final double shippingCharges = 520.0;
    private String uName, uPhone, uAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        cartManager = CartManager.getInstance();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        initWidgets();
        loadUserInfo();
        calculateOrderSummary();
    }

    private void initWidgets() {
        tvAddress = findViewById(R.id.tvCheckoutAddress);
        tvSubtotal = findViewById(R.id.tvCheckoutSubtotal);
        tvTotal = findViewById(R.id.tvCheckoutTotal);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEditAddress).setOnClickListener(v -> 
            startActivity(new Intent(this, EditUserProfileActivity.class)));

        findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> {
            UiUtils.animateClick(v);
            v.postDelayed(() -> {
                if (cartManager.getItems().isEmpty()) {
                    Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                placeOrder();
            }, 250);
        });
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uName = snapshot.child("name").getValue(String.class);
                uPhone = snapshot.child("phone").getValue(String.class);
                uAddress = snapshot.child("address").getValue(String.class);

                if (uAddress != null && !uAddress.isEmpty()) {
                    tvAddress.setText(uAddress);
                } else {
                    tvAddress.setText("No address provided. Please update in profile.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvAddress.setText("Failed to load user info.");
            }
        });
    }

    private void calculateOrderSummary() {
        double subtotal = cartManager.getTotalPrice();
        double total = subtotal + shippingCharges;

        tvSubtotal.setText(String.format(Locale.getDefault(), "Tk %.2f", subtotal));
        ((TextView) findViewById(R.id.tvCheckoutShipping)).setText(String.format(Locale.getDefault(), "Tk %.2f", shippingCharges));
        tvTotal.setText(String.format(Locale.getDefault(), "Tk %.2f", total));
    }

    private void placeOrder() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        RadioGroup rgPayment = findViewById(R.id.rgPaymentMethod);
        int selectedId = rgPayment.getCheckedRadioButtonId();
        android.widget.RadioButton rb = findViewById(selectedId);
        String paymentMethod = (rb != null) ? rb.getText().toString() : "COD";
        
        double subtotal = cartManager.getTotalPrice();
        double total = subtotal + shippingCharges;

        String orderId = dbRef.child("orders").push().getKey();
        Order order = new Order(orderId, uid, uName, uAddress, uPhone, paymentMethod, subtotal, shippingCharges, total, cartManager.getItems());

        if (orderId != null) {
            dbRef.child("orders").child(orderId).setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        // Log the interaction
                        LogManager.logAction("E-commerce", "Placed Order #" + orderId + " total " + total);

                        updateProductStats(order.items);
                        Toast.makeText(this, "Order placed successfully! Thank you.", Toast.LENGTH_LONG).show();
                        cartManager.clearCart();
                        
                        Intent intent = new Intent(this, ShopActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateProductStats(List<CartItem> items) {
        if (items == null) return;
        for (CartItem item : items) {
            dbRef.child("products").child(item.productId).runTransaction(new com.google.firebase.database.Transaction.Handler() {
                @NonNull
                @Override
                public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                    Product p = currentData.getValue(Product.class);
                    if (p != null) {
                        p.soldCount += item.quantity;
                        p.stockQuantity -= item.quantity;
                        currentData.setValue(p);
                    }
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable com.google.firebase.database.DatabaseError error, boolean committed, @Nullable com.google.firebase.database.DataSnapshot currentData) {
                    if (error != null) {
                        android.util.Log.e("CheckoutActivity", "Transaction failed: " + error.getMessage());
                    }
                }
            });
        }
    }
}
