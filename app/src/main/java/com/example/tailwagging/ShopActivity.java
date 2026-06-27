package com.example.tailwagging;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView rvRecommended, rvTopSelling, rvBrands;
    private EditText etSearch;
    private TextView tvRecommendedTitle;
    private LinearLayout catFood, catVet, catAcc, catIot;
    private MaterialCardView cardFood, cardVet, cardAcc, cardIot;
    
    private DatabaseReference dbRef;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> displayedProducts = new ArrayList<>();
    private final List<Product> topSellingProducts = new ArrayList<>();
    private final List<Brand> brands = new ArrayList<>();
    
    private ShopProductAdapter recommendedAdapter;
    private TopSellingAdapter topSellingAdapter;
    private BrandAdapter brandAdapter;
    private String currentCategory = "Food";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shopMainRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initWidgets();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        setupRecyclerViews();
        setupCategoryClickListeners();
        setupSearch();

        fetchProducts();
        NavbarHelper.setupNavbar(this);
    }

    private void initWidgets() {
        rvRecommended = findViewById(R.id.rvRecommendedProducts);
        rvTopSelling = findViewById(R.id.rvTopSelling);
        rvBrands = findViewById(R.id.rvBrands);
        etSearch = findViewById(R.id.etSearchShop);
        tvRecommendedTitle = findViewById(R.id.tvRecommendedTitle);

        catFood = findViewById(R.id.catFood);
        catVet = findViewById(R.id.catVet);
        catAcc = findViewById(R.id.catAcc);
        catIot = findViewById(R.id.catIot);

        cardFood = findViewById(R.id.cardFoodIcon);
        cardVet = findViewById(R.id.cardVetIcon);
        cardAcc = findViewById(R.id.cardAccIcon);
        cardIot = findViewById(R.id.cardIotIcon);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCart).setOnClickListener(v -> 
            startActivity(new Intent(this, CartActivity.class)));
    }

    private void setupRecyclerViews() {
        rvRecommended.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new ShopProductAdapter(displayedProducts);
        rvRecommended.setAdapter(recommendedAdapter);

        rvTopSelling.setLayoutManager(new LinearLayoutManager(this));
        topSellingAdapter = new TopSellingAdapter(topSellingProducts);
        rvTopSelling.setAdapter(topSellingAdapter);

        rvBrands.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        brandAdapter = new BrandAdapter(brands);
        rvBrands.setAdapter(brandAdapter);
        
        loadMockBrands();
    }

    private void loadMockBrands() {
        brands.clear();
        brands.add(new Brand("Josera", R.drawable.logo));
        brands.add(new Brand("NexGard", R.drawable.logo));
        brands.add(new Brand("Pedigree", R.drawable.logo));
        brands.add(new Brand("Ruffwear", R.drawable.logo));
        brandAdapter.notifyDataSetChanged();
    }

    private void setupCategoryClickListeners() {
        catFood.setOnClickListener(v -> selectCategory("Food"));
        catVet.setOnClickListener(v -> selectCategory("Vet Items"));
        catAcc.setOnClickListener(v -> selectCategory("Accessories"));
        catIot.setOnClickListener(v -> selectCategory("IOT Devices"));
    }

    private void selectCategory(String category) {
        currentCategory = category;
        
        // Update UI
        updateCategoryUI(cardFood, category.equals("Food"));
        updateCategoryUI(cardVet, category.equals("Vet Items"));
        updateCategoryUI(cardAcc, category.equals("Accessories"));
        updateCategoryUI(cardIot, category.equals("IOT Devices"));

        tvRecommendedTitle.setText("Recommended " + category);
        filterProducts(etSearch.getText().toString());
    }

    private void updateCategoryUI(MaterialCardView card, boolean active) {
        if (active) {
            card.setCardBackgroundColor(getColor(R.color.health_tab_inactive));
            ((android.widget.ImageView)card.getChildAt(0)).setColorFilter(getColor(R.color.dark_blue));
        } else {
            card.setCardBackgroundColor(getColor(R.color.white_overlay_medium));
            ((android.widget.ImageView)card.getChildAt(0)).setColorFilter(getColor(R.color.grey_dark));
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchProducts() {
        dbRef.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);
                    if (p != null) {
                        p.id = ds.getKey();
                        allProducts.add(p);
                    }
                }
                
                // Add mock products if database is empty for better initial experience
                if (allProducts.isEmpty()) {
                    addMockProducts();
                }
                
                // Sort for top selling
                topSellingProducts.clear();
                topSellingProducts.addAll(allProducts);
                topSellingProducts.sort((p1, p2) -> Integer.compare(p2.soldCount, p1.soldCount));
                if (topSellingProducts.size() > 5) {
                    topSellingProducts.subList(5, topSellingProducts.size()).clear();
                }
                topSellingAdapter.notifyDataSetChanged();
                
                filterProducts(etSearch.getText().toString());
            }

            private void addMockProducts() {
                allProducts.add(new Product("m1", "shop1", "Josera Mini Deluxe", "Food", 2900, 50, "", "900g", "Josera"));
                allProducts.add(new Product("m2", "shop1", "Pedigree Chicken", "Food", 5390, 30, "", "3kg", "Pedigree"));
                allProducts.add(new Product("m3", "shop2", "Warm fleece vest", "Accessories", 1780, 20, "", "Cozy winter wear", "Generic"));
                allProducts.add(new Product("m4", "shop3", "GPS Pet Tracker", "IOT Devices", 10500, 10, "", "Real-time tracking", "SmartLink"));
                allProducts.add(new Product("m5", "shop1", "Nexgard - Afoxolaner", "Vet Items", 3480, 40, "", "136mg", "NexGard"));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterProducts(String query) {
        displayedProducts.clear();
        for (Product p : allProducts) {
            boolean matchesCat = p.category != null && p.category.equalsIgnoreCase(currentCategory);
            boolean matchesSearch = query.isEmpty() || (p.name != null && p.name.toLowerCase().contains(query.toLowerCase()));
            
            if (matchesCat && matchesSearch) {
                displayedProducts.add(p);
            }
        }
        recommendedAdapter.notifyDataSetChanged();
    }
}
