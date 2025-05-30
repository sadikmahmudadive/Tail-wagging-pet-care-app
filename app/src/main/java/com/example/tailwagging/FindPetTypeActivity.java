package com.example.tailwagging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        ProgressDialog dialog = ProgressDialog.show(this, "", "Finding pet species...", true);

        OkHttpClient client = new OkHttpClient();

        try {
            Uri uri = Uri.parse(uriString);
            File file = createTempFileFromUri(uri);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            RequestBody.create(file, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://pet-type-finder-cnn-model.onrender.com/predict")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        textResult.setText("Failed to detect species.");
                    });
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    dialog.dismiss();
                    if (response.isSuccessful()) {
                        final String result = response.body().string();
                        runOnUiThread(() -> {
                            String species = parseSpeciesFromResult(result);
                            if (species != null) {
                                textResult.setText("Your Pet is a " + species);
                                // Return the result to AddEditPet
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("PREDICTED_SPECIES", species);
                                setResult(RESULT_OK, resultIntent);
                            } else {
                                textResult.setText("Unable to determine species.");
                            }
                        });
                    } else {
                        runOnUiThread(() -> textResult.setText("Failed to detect species."));
                    }
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
            textResult.setText("Failed to detect species.");
        }
    }

    private String parseSpeciesFromResult(String result) {
        // Very basic parser: expects {"species":"Persian Cat"}
        try {
            int start = result.indexOf(":\"") + 2;
            int end = result.indexOf("\"", start);
            if (start > 1 && end > start) {
                return result.substring(start, end);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Helper: Copy content from content:// uri to temp file for OkHttp
    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("pet_image", ".jpg", getCacheDir());
        tempFile.deleteOnExit();
        OutputStream outputStream = new java.io.FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    @Override
    public void finish() {
        super.finish();
    }
}