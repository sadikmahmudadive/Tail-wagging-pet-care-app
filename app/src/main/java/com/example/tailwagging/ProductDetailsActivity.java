package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private Product product;
    private int quantity = 1;
    private TextView tvQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);

        product = (Product) getIntent().getSerializableExtra("SELECTED_PRODUCT");
        if (product == null) {
            finish();
            return;
        }

        initWidgets();
        displayProductDetails();
    }

    private void initWidgets() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCart).setOnClickListener(v -> 
            startActivity(new Intent(this, CartActivity.class)));

        tvQuantity = findViewById(R.id.tvQuantity);
        ImageButton btnPlus = findViewById(R.id.btnPlus);
        ImageButton btnMinus = findViewById(R.id.btnMinus);

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        findViewById(R.id.btnAddToCartDetail).setOnClickListener(v -> {
            CartManager.getInstance().addItem(product, quantity);
            Toast.makeText(this, quantity + " item(s) added to cart!", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProductDetails() {
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(product.name);
        ((TextView) findViewById(R.id.tvProductName)).setText(product.name);
        ((TextView) findViewById(R.id.tvProductBrand)).setText("Brand: " + (product.brand != null ? product.brand : "Generic"));
        ((TextView) findViewById(R.id.tvProductPrice)).setText(String.format(Locale.getDefault(), "Tk %.2f", product.price));
        ((TextView) findViewById(R.id.tvProductDesc)).setText(product.description);
        
        // Mock rating for now
        ((RatingBar) findViewById(R.id.ratingBar)).setRating(4.5f);
        ((TextView) findViewById(R.id.tvRatingValue)).setText("4.5");
        ((TextView) findViewById(R.id.tvReviewCount)).setText("(89 reviews)");

        ImageView ivMain = findViewById(R.id.ivProductMain);
        Glide.with(this)
                .load(product.imageUrl)
                .placeholder(R.drawable.pet1)
                .into(ivMain);

        // Pet tag - use category or brand for now as mock
        ((TextView) findViewById(R.id.tvRecPetTag)).setText(product.category);
    }
}
