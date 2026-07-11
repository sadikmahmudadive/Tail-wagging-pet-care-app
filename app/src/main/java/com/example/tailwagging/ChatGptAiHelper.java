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
 * Helper class to integrate with OpenAI's ChatGPT (Vision) API to analyze pet images.
 */
public class ChatGptAiHelper {

    private static final String TAG = "ChatGptAiHelper";
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;
    private static final String OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    public interface GeminiCallback { // Keeping name to minimize ripple changes in callers
        void onSuccess(String analysis);
        void onFailure(String errorMessage);
    }

    /**
     * Analyzes a pet image using ChatGPT Vision (gpt-4o-mini) with a custom prompt.
     */
    public static void analyzePetImage(@NonNull Context context, @NonNull Uri imageUri, @NonNull String prompt, @NonNull GeminiCallback callback) {
        // API_KEY always non-empty due to fallback above

        String base64Image = encodeImageToBase64(context, imageUri);
        if (base64Image == null) {
            callback.onFailure("Could not process image data.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        try {
            // Build a Chat Completions request with text + image (as data URL)
            JSONObject root = new JSONObject();
            root.put("model", "gpt-4o-mini");

            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");

            JSONArray content = new JSONArray();

            JSONObject textBlock = new JSONObject();
            textBlock.put("type", "text");
            textBlock.put("text", prompt);
            content.put(textBlock);

            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);

            JSONObject imageBlock = new JSONObject();
            imageBlock.put("type", "image_url");
            imageBlock.put("image_url", imageUrl);
            content.put(imageBlock);

            userMsg.put("content", content);
            messages.put(userMsg);

            root.put("messages", messages);

            RequestBody body = RequestBody.create(
                    root.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(OPENAI_CHAT_COMPLETIONS_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
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
                            String errorMsg = "OpenAI Error: " + response.code();
                            // Try to extract message
                            try {
                                JSONObject err = new JSONObject(rawJson).optJSONObject("error");
                                if (err != null) errorMsg = err.optString("message", errorMsg);
                            } catch (Exception ignored) { }
                            callback.onFailure(errorMsg);
                            return;
                        }

                        JSONObject json = new JSONObject(rawJson);
                        JSONArray choices = json.optJSONArray("choices");
                        if (choices != null && choices.length() > 0) {
                            JSONObject message = choices.getJSONObject(0).optJSONObject("message");
                            String aiContent = message != null ? message.optString("content", "") : "";
                            if (aiContent == null) aiContent = "";
                            if (aiContent.isEmpty()) {
                                callback.onFailure("AI returned empty result. Please try another photo.");
                            } else {
                                callback.onSuccess(aiContent.trim());
                            }
                        } else {
                            callback.onFailure("No choices returned by AI.");
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

    /**
     * Generates text response using ChatGPT (gpt-4o-mini) based on a text prompt.
     */
    public static void generateText(@NonNull String prompt, @NonNull GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject root = new JSONObject();
            root.put("model", "gpt-4o-mini");

            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.put(userMsg);

            root.put("messages", messages);

            RequestBody body = RequestBody.create(
                    root.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(OPENAI_CHAT_COMPLETIONS_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        String rawJson = responseBody != null ? responseBody.string() : "";
                        if (!response.isSuccessful()) {
                            callback.onFailure("OpenAI Error: " + response.code());
                            return;
                        }

                        JSONObject json = new JSONObject(rawJson);
                        JSONArray choices = json.optJSONArray("choices");
                        if (choices != null && choices.length() > 0) {
                            JSONObject message = choices.getJSONObject(0).optJSONObject("message");
                            String aiContent = message != null ? message.optString("content", "") : "";
                            callback.onSuccess(aiContent.trim());
                        } else {
                            callback.onFailure("No response from AI.");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parsing error.");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onFailure("Request construction error.");
        }
    }

    private static String encodeImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            // Resize to ~1024px max for speed/bandwidth
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
