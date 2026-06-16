package com.example.tailwagging;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvSelectedAddress;
    private FloatingActionButton fabCurrentLocation;
    private EditText etSearch;
    private View btnClearSearch;
    private LatLng selectedLatLng;
    private String selectedAddress;

    // Use a more detailed open-source style (OpenFreeMap or Carto)
    private static final String STYLE_URL = "https://tiles.openfreemap.org/styles/bright";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MapLibre
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_location_picker);

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        Button btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fabCurrentLocation = findViewById(R.id.fabCurrentLocation);
        etSearch = findViewById(R.id.etSearchLocation);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fabCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("LATITUDE", selectedLatLng.getLatitude());
                resultIntent.putExtra("LONGITUDE", selectedLatLng.getLongitude());
                resultIntent.putExtra("ADDRESS", selectedAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });

        setupSearch();
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchLocation(etSearch.getText().toString());
                return true;
            }
            return false;
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void searchLocation(String query) {
        if (query.isEmpty()) return;

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng target = new LatLng(address.getLatitude(), address.getLongitude());
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15));
                }
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        this.mapLibreMap = mapLibreMap;

        mapLibreMap.setStyle(new Style.Builder().fromUri(STYLE_URL), style -> {
            // Default location: Colombo, Sri Lanka
            LatLng defaultLocation = new LatLng(6.9271, 79.8612);
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14));

            mapLibreMap.addOnCameraIdleListener(() -> {
                selectedLatLng = mapLibreMap.getCameraPosition().target;
                updateAddress(selectedLatLng);
            });

            if (checkPermission()) {
                moveToCurrentLocation();
            } else {
                requestPermission();
            }
        });
    }

    private void moveToCurrentLocation() {
        if (!checkPermission()) return;
        
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && mapLibreMap != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 16));
                } else {
                    Toast.makeText(this, "Could not get current location. Ensure GPS is on.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedAddress = addresses.get(0).getAddressLine(0);
                tvSelectedAddress.setText(selectedAddress);
            } else {
                selectedAddress = "Unknown Address";
                tvSelectedAddress.setText(selectedAddress);
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Failed to get address", e);
            tvSelectedAddress.setText("Error fetching address");
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mapLibreMap != null) moveToCurrentLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}