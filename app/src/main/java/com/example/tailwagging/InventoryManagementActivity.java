package com.example.tailwagging;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManagementActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private static final int PICK_IMAGE_REQUEST = 101;
    private RecyclerView rvProducts;
    private ProgressBar pbLoading;
    private EditText etSearch;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> allProducts = new ArrayList<>();
    private DatabaseReference dbRef;
    private String shopId;
    
    private Uri imageUri;
    private ImageView ivPreview;
    private String currentImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory_management);

        shopId = getIntent().getStringExtra("SHOP_ID");
        if (shopId == null) {
            shopId = FirebaseAuth.getInstance().getUid();
        }
        
        String shopName = getIntent().getStringExtra("SHOP_NAME");
        if (shopName != null) {
            ((TextView) findViewById(R.id.tvInventoryTitle)).setText(shopName + " Inventory");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventoryRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shopId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        rvProducts = findViewById(R.id.rvProducts);
        pbLoading = findViewById(R.id.pbInventory);
        etSearch = findViewById(R.id.etSearchProducts);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.fabAddProduct).setOnClickListener(v -> showAddEditProductDialog(null));

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this);
        rvProducts.setAdapter(adapter);

        fetchProducts();
        setupSearch();
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

    private void filterProducts(String query) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.name != null && p.name.toLowerCase().contains(query.toLowerCase())) {
                    productList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchProducts() {
        if (shopId == null) return;
        pbLoading.setVisibility(View.VISIBLE);
        dbRef.child("products").orderByChild("shopId").equalTo(shopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allProducts.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Product product = ds.getValue(Product.class);
                            if (product != null) {
                                product.id = ds.getKey();
                                allProducts.add(product);
                            }
                        }
                        filterProducts(etSearch.getText().toString());
                        pbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(InventoryManagementActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etBrand = dialogView.findViewById(R.id.etProductBrand);
        EditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        EditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etStock = dialogView.findViewById(R.id.etProductStock);
        EditText etDesc = dialogView.findViewById(R.id.etProductDescription);
        ivPreview = dialogView.findViewById(R.id.ivProductPreview);
        View btnSelectImage = dialogView.findViewById(R.id.btnSelectProductImage);
        View btnSave = dialogView.findViewById(R.id.btnSaveProduct);

        imageUri = null;
        currentImageUrl = "";

        if (product != null) {
            tvTitle.setText("Edit Product");
            etName.setText(product.name);
            etBrand.setText(product.brand);
            etCategory.setText(product.category);
            etPrice.setText(String.valueOf(product.price));
            etStock.setText(String.valueOf(product.stockQuantity));
            etDesc.setText(product.description);
            currentImageUrl = product.imageUrl;
            
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Glide.with(this).load(currentImageUrl).into(ivPreview);
                ivPreview.setAlpha(1.0f);
            }
        }

        btnSelectImage.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String brand = etBrand.getText().toString().trim();
            String cat = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (name.isEmpty() || cat.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Saving Product...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            if (imageUri != null) {
                // Upload new image
                MediaManager.get().upload(imageUri)
                        .unsigned("tail_wagging")
                        .option("folder", "products/")
                        .callback(new UploadCallback() {
                            @Override public void onStart(String requestId) {}
                            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                            @Override public void onSuccess(String requestId, Map resultData) {
                                String uploadedUrl = (String) resultData.get("secure_url");
                                saveProductToFirebase(product, name, brand, cat, price, stock, desc, uploadedUrl, progressDialog, dialog);
                            }
                            @Override public void onError(String requestId, ErrorInfo error) {
                                progressDialog.dismiss();
                                Toast.makeText(InventoryManagementActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();
            } else {
                // Use existing image
                saveProductToFirebase(product, name, brand, cat, price, stock, desc, currentImageUrl, progressDialog, dialog);
            }
        });

        dialog.show();
    }

    private void saveProductToFirebase(Product product, String name, String brand, String cat, double price, int stock, String desc, String imageUrl, ProgressDialog pd, AlertDialog dialog) {
        String productId = (product == null) ? dbRef.child("products").push().getKey() : product.id;
        Product newProduct = new Product(productId, shopId, name, cat, price, stock, imageUrl, desc, brand);

        if (productId != null) {
            dbRef.child("products").child(productId).setValue(newProduct)
                    .addOnSuccessListener(aVoid -> {
                        pd.dismiss();
                        Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivPreview != null) {
                Glide.with(this).load(imageUri).into(ivPreview);
                ivPreview.setAlpha(1.0f);
            }
        }
    }

    @Override
    public void onEdit(Product product) {
        showAddEditProductDialog(product);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbRef.child("products").child(product.id).removeValue();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
