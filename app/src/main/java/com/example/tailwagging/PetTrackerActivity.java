package com.example.tailwagging;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class PetTrackerActivity extends AppCompatActivity {

    private TextView tvHeartRate, tvTemperature;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_tracker);

        tvHeartRate = findViewById(R.id.tvHeartRate);
        tvTemperature = findViewById(R.id.tvTemperature);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        startVitalSimulation();
        NavbarHelper.setupNavbar(this);
    }

    private void startVitalSimulation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int heartRate = 70 + random.nextInt(30);
                double temp = 37.5 + (random.nextDouble() * 2);
                
                tvHeartRate.setText(heartRate + " BPM");
                tvTemperature.setText(String.format("%.1f °C", temp));
                
                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
