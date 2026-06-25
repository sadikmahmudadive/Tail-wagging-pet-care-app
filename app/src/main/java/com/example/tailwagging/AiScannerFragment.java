package com.example.tailwagging;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class AiScannerFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 105;
    private ImageView ivScanPreview;
    private View layoutUploadPrompt, cardResult;
    private ProgressBar progressBarAi;
    private TextView tvDiagnosis, tvSuggestion;
    private Pet selectedPet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedPet = getArguments().getParcelable("SELECTED_PET");
        }

        ivScanPreview = view.findViewById(R.id.ivScanPreview);
        layoutUploadPrompt = view.findViewById(R.id.layoutUploadPrompt);
        cardResult = view.findViewById(R.id.cardResult);
        progressBarAi = view.findViewById(R.id.progressBarAi);
        tvDiagnosis = view.findViewById(R.id.tvDiagnosis);
        tvSuggestion = view.findViewById(R.id.tvSuggestion);

        view.findViewById(R.id.cardUploadContainer).setOnClickListener(v -> openImagePicker());
        view.findViewById(R.id.btnBookVet).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PetServicesActivity.class);
            startActivity(intent);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri == null) return;

            Glide.with(this).load(imageUri).into(ivScanPreview);
            ivScanPreview.setAlpha(1.0f);
            layoutUploadPrompt.setVisibility(View.GONE);
            cardResult.setVisibility(View.GONE);
            progressBarAi.setVisibility(View.VISIBLE);

            String prompt = "You are a professional veterinary assistant. Analyze this photo of a pet's skin/body. " +
                    "Identify potential health issues (e.g. Dermatitis, Fleas, Fungal Infection). " +
                    "Provide your response in this exact format: " +
                    "DIAGNOSIS: [Name of potential issue] | SUGGESTION: [Short advice for the owner]";

            GeminiAiHelper.analyzePetImage(getContext(), imageUri, prompt, new GeminiAiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String analysis) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        progressBarAi.setVisibility(View.GONE);
                        cardResult.setVisibility(View.VISIBLE);

                        if (analysis.contains("|")) {
                            String[] parts = analysis.split("\\|");
                            tvDiagnosis.setText(parts[0].replace("DIAGNOSIS:", "").trim());
                            tvSuggestion.setText(parts[1].replace("SUGGESTION:", "").trim());
                        } else {
                            tvDiagnosis.setText("AI Analysis Result");
                            tvSuggestion.setText(analysis);
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        progressBarAi.setVisibility(View.GONE);
                        layoutUploadPrompt.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
}