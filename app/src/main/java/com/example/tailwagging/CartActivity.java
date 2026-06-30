package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private View layoutEmpty, layoutSummary;
    private CartAdapter adapter;
    private CartManager cartManager;
    private final double shippingCharges = 520.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();
        initWidgets();
        setupRecyclerView();
        updateUI();
        
        UiUtils.fadeIn(findViewById(R.id.rvCartItems));
    }

    private void initWidgets() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        layoutEmpty = findViewById(R.id.layoutEmptyCart);
        layoutSummary = findViewById(R.id.layoutSummary);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            UiUtils.animateClick(v);
            v.postDelayed(() -> {
                if (cartManager.getItems().isEmpty()) {
                    Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, CheckoutActivity.class));
                }
            }, 250);
        });
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartManager.getItems(), this);
        rvCartItems.setAdapter(adapter);
    }

    private void updateUI() {
        if (cartManager.getItems().isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            layoutSummary.setVisibility(View.GONE);
            findViewById(R.id.btnCheckout).setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            layoutSummary.setVisibility(View.VISIBLE);
            findViewById(R.id.btnCheckout).setVisibility(View.VISIBLE);

            double subtotal = cartManager.getTotalPrice();
            double total = subtotal + shippingCharges;

            tvSubtotal.setText(String.format(Locale.getDefault(), "Tk %.2f", subtotal));
            tvShipping.setText(String.format(Locale.getDefault(), "Tk %.2f", shippingCharges));
            tvTotal.setText(String.format(Locale.getDefault(), "Tk %.2f", total));
        }
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        cartManager.updateQuantity(position, newQuantity);
        adapter.notifyItemChanged(position);
        updateUI();
    }

    @Override
    public void onRemove(int position) {
        cartManager.removeItem(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, cartManager.getItems().size());
        updateUI();
    }
}
