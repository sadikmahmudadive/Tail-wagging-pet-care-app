package com.example.tailwagging;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private EditText etContent;
    private AutoCompleteTextView etPostType;
    private ImageView ivSelected;
    private Uri imageUri;
    private DatabaseReference dbRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();

        etContent = findViewById(R.id.etPostContent);
        etPostType = findViewById(R.id.etPostType);
        ivSelected = findViewById(R.id.ivSelectedImage);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSelectImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        String[] categories = {"MOMENT", "ADOPTION", "RESCUE"};
        etPostType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories));
        etPostType.setOnClickListener(v -> etPostType.showDropDown());

        findViewById(R.id.btnPublishPost).setOnClickListener(v -> moderateAndPublish());
    }

    private void moderateAndPublish() {
        String content = etContent.getText().toString().trim();
        String type = etPostType.getText().toString().trim();

        if (content.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Analyzing content...");
        pd.setCancelable(false);
        pd.show();

        // AI Moderation: Ensure content is pet-related and appropriate
        String prompt = "Act as a pet community moderator. Analyze the following post content and category. " +
                "If it is related to pets (dogs, cats, etc.) and is appropriate, return 'APPROVED'. " +
                "If it is unrelated to pets or contains inappropriate language, return a friendly reason why it cannot be posted. " +
                "Post Category: " + type + "\n" +
                "Post Content: " + content;

        ChatGptAiHelper.generateText(prompt, new ChatGptAiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String analysis) {
                runOnUiThread(() -> {
                    if ("APPROVED".equalsIgnoreCase(analysis.trim())) {
                        pd.setMessage("Publishing post...");
                        uploadAndSave(pd, content, type);
                    } else {
                        pd.dismiss();
                        new AlertDialog.Builder(CreatePostActivity.this)
                                .setTitle("Post Moderation")
                                .setMessage(analysis)
                                .setPositiveButton("Edit", null)
                                .show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // Fallback to manual if AI fails
                    uploadAndSave(pd, content, type);
                });
            }
        });
    }

    private void uploadAndSave(ProgressDialog pd, String content, String type) {
        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                    .unsigned("tail_wagging")
                    .option("folder", "community/")
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override public void onSuccess(String requestId, Map resultData) {
                            savePostToFirebase((String) resultData.get("secure_url"), content, type, pd);
                        }
                        @Override public void onError(String requestId, ErrorInfo error) {
                            pd.dismiss();
                            Toast.makeText(CreatePostActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } else {
            savePostToFirebase(null, content, type, pd);
        }
    }

    private void savePostToFirebase(String imageUrl, String content, String type, ProgressDialog pd) {
        dbRef.child("users").child(user.getUid()).get().addOnSuccessListener(snapshot -> {
            String name = snapshot.child("name").getValue(String.class);
            String photo = snapshot.child("photoUrl").getValue(String.class);
            
            String postId = dbRef.child("community_posts").push().getKey();
            FeedPost post = new FeedPost(postId, user.getUid(), name, photo, type, content, imageUrl);
            
            if (postId != null) {
                dbRef.child("community_posts").child(postId).setValue(post)
                        .addOnSuccessListener(aVoid -> {
                            awardPointsForPosting(user.getUid());
                            pd.dismiss();
                            Toast.makeText(this, "Post shared with community!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            pd.dismiss();
                            Toast.makeText(this, "Failed to publish", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void awardPointsForPosting(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        userRef.child("points").get().addOnSuccessListener(snapshot -> {
            Long currentPoints = snapshot.getValue(Long.class);
            if (currentPoints == null) currentPoints = 0L;
            userRef.child("points").setValue(currentPoints + 5); // Increased reward for high-quality posts
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivSelected.setImageURI(imageUri);
            ivSelected.setVisibility(android.view.View.VISIBLE);
        }
    }
}