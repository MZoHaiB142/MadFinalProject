package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etBody;
    private Button btnPost;
    private TextView btnClose;

    // Naye tags ke variables
    private TextView tagGeneral, tagVisa, tagIelts, tagUniLife;
    private String selectedCategory = Constants.CATEGORY_GENERAL; // Default category

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etTitle = findViewById(R.id.etPostTitle);
        etBody = findViewById(R.id.etPostBody);
        btnPost = findViewById(R.id.btnPost);
        btnClose = findViewById(R.id.btnClose);

        // Initialize Tags
        tagGeneral = findViewById(R.id.tagGeneral);
        tagVisa = findViewById(R.id.tagVisa);
        tagIelts = findViewById(R.id.tagIelts);
        tagUniLife = findViewById(R.id.tagUniLife);

        // Setup Tag Clicks
        tagGeneral.setOnClickListener(v -> selectTag(tagGeneral, Constants.CATEGORY_GENERAL));
        tagVisa.setOnClickListener(v -> selectTag(tagVisa, Constants.CATEGORY_VISA_HELP));
        tagIelts.setOnClickListener(v -> selectTag(tagIelts, Constants.CATEGORY_IELTS_PREP));
        tagUniLife.setOnClickListener(v -> selectTag(tagUniLife, Constants.CATEGORY_UNIVERSITY_LIFE));

        // Default selection
        selectTag(tagGeneral, Constants.CATEGORY_GENERAL);

        // Check if editing
        boolean editMode = getIntent().getBooleanExtra("editMode", false);
        if (editMode) {
            String postId = getIntent().getStringExtra("postId");
            String title = getIntent().getStringExtra("title");
            String body = getIntent().getStringExtra("body");
            String category = getIntent().getStringExtra("category");

            if (title != null) etTitle.setText(title);
            if (body != null) etBody.setText(body);

            // Set category based on edit data
            if (category != null) {
                if (category.equals(Constants.CATEGORY_VISA_HELP)) selectTag(tagVisa, category);
                else if (category.equals(Constants.CATEGORY_IELTS_PREP)) selectTag(tagIelts, category);
                else if (category.equals(Constants.CATEGORY_UNIVERSITY_LIFE)) selectTag(tagUniLife, category);
                else selectTag(tagGeneral, Constants.CATEGORY_GENERAL);
            }
            btnPost.setText("Update Question");
        }

        // Close button (X)
        btnClose.setOnClickListener(v -> finish());

        // Post button
        btnPost.setOnClickListener(v -> {
            if (editMode) {
                updatePost();
            } else {
                createPost();
            }
        });
    }

    // 🔥 Tag selection aur UI update ka logic
    private void selectTag(TextView selectedView, String categoryName) {
        selectedCategory = categoryName;

        // Sabko pehle unselected (gray) kar do
        resetTagUI(tagGeneral);
        resetTagUI(tagVisa);
        resetTagUI(tagIelts);
        resetTagUI(tagUniLife);

        // Jo select hua hai usko blue kar do
        selectedView.setBackgroundResource(R.drawable.bg_tag_selected);
        selectedView.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void resetTagUI(TextView tag) {
        tag.setBackgroundResource(R.drawable.bg_tag_unselected);
        tag.setTextColor(Color.parseColor("#4B5563"));
    }

    private void createPost() {
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(body)) {
            etBody.setError("Details are required");
            etBody.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = user.getUid();
        String userEmail = user.getEmail();

        // Fetch user details from Firestore
        db.collection(Constants.DB_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "User";
                    String role = "Student";

                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString(Constants.KEY_FULL_NAME);
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            userName = fetchedName;
                        }

                        // Role based on Qualification
                        String fetchedRole = documentSnapshot.getString(Constants.KEY_QUALIFICATION);
                        if (fetchedRole != null && !fetchedRole.isEmpty()) {
                            role = fetchedRole;
                        }
                    }

                    // Create post object (Spinner ki jagah ab selectedCategory ja raha hai)
                    CommunityPost post = new CommunityPost(userId, userName, userEmail, role, title, body, selectedCategory);

                    // Generate New Document ID
                    DocumentReference newPostRef = db.collection(Constants.DB_COMMUNITY_POSTS).document();
                    post.postId = newPostRef.getId();

                    // Save to Firestore
                    newPostRef.set(post)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CreatePostActivity.this, "Question posted successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                LogUtils.e("CreatePostActivity", "Error creating post", e);
                                Toast.makeText(CreatePostActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    LogUtils.e("CreatePostActivity", "Error fetching user data", e);
                    Toast.makeText(CreatePostActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePost() {
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();
        String postId = getIntent().getStringExtra("postId");

        // Validation
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(body)) {
            etBody.setError("Details are required");
            etBody.requestFocus();
            return;
        }

        if (postId == null) {
            Toast.makeText(this, "Post ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to update post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Update post in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("body", body);
        updates.put("category", selectedCategory); // 🔥 Updated category logic

        db.collection(Constants.DB_COMMUNITY_POSTS).document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePostActivity.this, "Question updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    LogUtils.e("CreatePostActivity", "Error updating post", e);
                    Toast.makeText(CreatePostActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}