package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// ✅ Firestore Imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private EditText etComment;
    private ImageButton btnSend, btnBack;
    private TextView tvPostTitle, tvEmptyState;
    private CommentsAdapter adapter;
    private List<Comment> commentsList;

    private String postId;
    private String postTitle;

    private FirebaseAuth mAuth;
    // ✅ Firestore Reference
    private FirebaseFirestore db;
    // ✅ Listener Registration
    private ListenerRegistration commentsListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Get post ID from intent
        postId = getIntent().getStringExtra("postId");
        postTitle = getIntent().getStringExtra("postTitle");

        if (postId == null) {
            Toast.makeText(this, "Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        // ✅ Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        tvPostTitle = findViewById(R.id.tvPostTitle);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSend);

        // Set post title
        if (postTitle != null) {
            tvPostTitle.setText(postTitle);
        }

        // Initialize RecyclerView
        commentsList = new ArrayList<>();
        adapter = new CommentsAdapter(commentsList, mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Send button
        btnSend.setOnClickListener(v -> addComment());

        // Load comments
        loadComments();
    }

    private void loadComments() {
        if (postId == null) return;

        // Remove previous listener if exists
        if (commentsListener != null) {
            commentsListener.remove();
        }

        // ✅ Firestore Query: Collection "comments" where postId == currentPostId
        commentsListener = db.collection(Constants.DB_COMMENTS)
                .whereEqualTo("postId", postId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            LogUtils.e("CommentsActivity", "Listen failed.", error);
                            Toast.makeText(CommentsActivity.this, "Error loading comments", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        commentsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Comment comment = doc.toObject(Comment.class);
                                comment.commentId = doc.getId(); // Set Firestore ID
                                commentsList.add(comment);
                            } catch (Exception e) {
                                LogUtils.e("CommentsActivity", "Error parsing comment", e);
                            }
                        }

                        // Sort by timestamp (oldest first)
                        Collections.sort(commentsList, (c1, c2) -> Long.compare(c1.timestamp, c2.timestamp));

                        adapter.updateList(commentsList);

                        // Show/hide empty state
                        if (commentsList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerViewComments.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            recyclerViewComments.setVisibility(View.VISIBLE);
                        }

                        // Scroll to bottom
                        if (commentsList.size() > 0) {
                            recyclerViewComments.smoothScrollToPosition(commentsList.size() - 1);
                        }
                    }
                });
    }

    private void addComment() {
        String commentText = etComment.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            etComment.setError("Please enter a comment");
            etComment.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String userEmail = user.getEmail();

        // Get user details from Firestore
        db.collection(Constants.DB_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "User";
                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString(Constants.KEY_FULL_NAME);
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            userName = fetchedName;
                        } else {
                            userName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                        }
                    } else {
                        userName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    }

                    // Create comment
                    Comment comment = new Comment(postId, userId, userName, userEmail, commentText);

                    // ✅ Generate New ID
                    DocumentReference newCommentRef = db.collection(Constants.DB_COMMENTS).document();
                    comment.commentId = newCommentRef.getId();

                    // ✅ Save to Firestore
                    newCommentRef.set(comment)
                            .addOnSuccessListener(aVoid -> {
                                etComment.setText("");
                                // Update comment count in post using Firestore Increment
                                updatePostCommentCount(1);
                                Toast.makeText(CommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                LogUtils.e("CommentsActivity", "Error adding comment", e);
                                Toast.makeText(CommentsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    LogUtils.e("CommentsActivity", "Error fetching user data", e);
                });
    }

    private void updatePostCommentCount(int increment) {
        if (postId == null) return;

        // ✅ Firestore Atomic Increment
        db.collection(Constants.DB_COMMUNITY_POSTS).document(postId)
                .update("commentCount", FieldValue.increment(increment))
                .addOnFailureListener(e -> {
                    LogUtils.e("CommentsActivity", "Error updating comment count", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }
}