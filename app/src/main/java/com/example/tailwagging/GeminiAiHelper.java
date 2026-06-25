package com.example.tailwagging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class to integrate with Google's Gemini API.
 */
public class GeminiAiHelper {
    
    private static final String TAG = "GeminiAiHelper";
    // TODO: Replace with your actual Gemini API Key
    private static final String API_KEY = "AQ.Ab8RN6JAAp0mC5tRS4R_RDrr1pB4mgbzQ7WxJSDyex9C5vlm0w";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String analysis);
        void onFailure(String errorMessage);
    }

    /**
     * Analyzes a pet image using Gemini 3.5 Flash with a custom prompt.
     */
    public static void analyzePetImage(@NonNull Context context, @NonNull Uri imageUri, @NonNull String prompt, @NonNull GeminiCallback callback) {
        if ("YOUR_GEMINI_API_KEY".equals(API_KEY)) {
            callback.onFailure("API Key not configured. Please set your Gemini API key in GeminiAiHelper.java");
            return;
        }

        if (API_KEY.startsWith("AQ.")) {
            Log.w(TAG, "Potentially Invalid API Key: Your key starts with 'AQ.', which is typical for xAI (Grok). Gemini keys usually start with 'AIza'. We will proceed, but this may cause a 403 error.");
        }

        String base64Image = encodeImageToBase64(context, imageUri);
        if (base64Image == null) {
            callback.onFailure("Could not process image data.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        try {
            // Gemini JSON structure
            JSONObject root = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();

            // Text part
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            // Image part
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.put(imagePart);

            content.put("parts", parts);
            contents.put(content);
            root.put("contents", contents);

            RequestBody body = RequestBody.create(
                    root.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(GEMINI_API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network Error: " + e.getMessage(), e);
                    callback.onFailure("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        String rawJson = responseBody != null ? responseBody.string() : "";
                        Log.d(TAG, "Raw Response: " + rawJson);
                        
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "Server Error: " + response.code() + " - " + rawJson);
                            String errorMsg = "Server Error: " + response.code();
                            if (response.code() == 503) {
                                errorMsg = "Gemini is busy (High Demand). Please try again in a few seconds.";
                            } else if (rawJson.contains("API_KEY_INVALID")) {
                                errorMsg = "Invalid API Key. Please check your key in Google AI Studio.";
                            } else if (rawJson.contains("MODEL_NOT_FOUND")) {
                                errorMsg = "AI Model not found. We are trying to reach the latest server.";
                            }
                            callback.onFailure(errorMsg);
                            return;
                        }

                        JSONObject jsonResponse = new JSONObject(rawJson);
                        // Gemini response path: candidates[0].content.parts[0].text
                        JSONArray candidates = jsonResponse.optJSONArray("candidates");
                        
                        if (candidates != null && candidates.length() > 0) {
                            String aiContent = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            
                            Log.d(TAG, "AI Content: " + aiContent);
                            callback.onSuccess(aiContent);
                        } else {
                            // Check for safety filters or blocked content
                            String blockReason = "";
                            if (jsonResponse.has("promptFeedback")) {
                                blockReason = " (Content blocked by safety filters)";
                            }
                            Log.e(TAG, "No candidates in response: " + rawJson);
                            callback.onFailure("AI could not generate a result. Please try another photo." + blockReason);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error", e);
                        callback.onFailure("Failed to parse AI response.");
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error building JSON request", e);
            callback.onFailure("Internal error building request.");
        }
    }

    private static String encodeImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            // Gemini handles large images well, but resizing to 1024 for speed/bandwidth
            int maxSize = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap processedBitmap;
            
            if (width > maxSize || height > maxSize) {
                float scale = (float) maxSize / Math.max(width, height);
                processedBitmap = Bitmap.createScaledBitmap(bitmap, Math.round(width * scale), Math.round(height * scale), true);
            } else {
                processedBitmap = bitmap;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            if (processedBitmap != bitmap) {
                processedBitmap.recycle();
            }
            bitmap.recycle();

            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}
