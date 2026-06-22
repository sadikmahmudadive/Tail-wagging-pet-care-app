package com.example.tailwagging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;

import java.io.IOException;
import okhttp3.*;

public class FindPetTypeActivity extends Activity {

    private ImageView imageViewPet;
    private Button btnFindPetType;
    private TextView textResult;
    private String petImageUriString; // Passed as Intent extra

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pet_type);

        imageViewPet = findViewById(R.id.imageViewPet);
        btnFindPetType = findViewById(R.id.btnFindPetType);
        textResult = findViewById(R.id.textResult);

        petImageUriString = getIntent().getStringExtra("PET_IMAGE_URI");

        if (petImageUriString != null) {
            Glide.with(this)
                    .load(Uri.parse(petImageUriString))
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .into(imageViewPet);
        }

        btnFindPetType.setOnClickListener(v -> {
            if (petImageUriString != null) {
                findPetSpecies(petImageUriString);
            } else {
                Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            }
        });

        // Automatically run prediction when opened with image
        if (petImageUriString != null) {
            findPetSpecies(petImageUriString);
        }
    }

    private void findPetSpecies(String uriString) {
        ProgressDialog dialog = ProgressDialog.show(this, "", "Finding pet species via Gemini AI...", true);

        Uri uri = Uri.parse(uriString);
        String prompt = "Identify the breed or species of the pet in this photo. Be specific (e.g. Golden Retriever, Persian Cat). " +
                "Respond ONLY with the name of the breed/species.";

        GeminiAiHelper.analyzePetImage(this, uri, prompt, new GeminiAiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String analysis) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    String species = analysis.trim();
                    textResult.setText("Your Pet is a " + species);
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("PREDICTED_SPECIES", species);
                    setResult(RESULT_OK, resultIntent);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FindPetTypeActivity", "Gemini Error: " + errorMessage);
                runOnUiThread(() -> {
                    dialog.dismiss();
                    textResult.setText("Failed to detect species: " + errorMessage);
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
    }
}