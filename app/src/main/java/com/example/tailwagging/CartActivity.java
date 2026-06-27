package com.example.tailwagging;

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
    private TextView tvTotalPrice;
    private View layoutEmpty;
    private CartAdapter adapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();
        initWidgets();
        setupRecyclerView();
        updateUI();
    }

    private void initWidgets() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        layoutEmpty = findViewById(R.id.layoutEmptyCart);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartManager.getItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Confirm Order")
                        .setMessage("Total Amount: " + String.format(Locale.getDefault(), "Tk %.2f", cartManager.getTotalPrice()))
                        .setPositiveButton("Place Order", (dialog, which) -> {
                            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                            cartManager.clearCart();
                            updateUI();
                            adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
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
            findViewById(R.id.cardCheckout).setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            findViewById(R.id.cardCheckout).setVisibility(View.VISIBLE);
            tvTotalPrice.setText(String.format(Locale.getDefault(), "Tk %.2f", cartManager.getTotalPrice()));
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
