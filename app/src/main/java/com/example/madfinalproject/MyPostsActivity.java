package com.example.madfinalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.MyPostsAdapter;
import com.example.madfinalproject.CommunityPost;
import com.example.madfinalproject.utils.Constants; // ✅ Added Import
import com.example.madfinalproject.utils.LogUtils; // ✅ Added LogUtils for debugging
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity implements MyPostsAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private MyPostsAdapter adapter;
    private List<CommunityPost> myPostsList;
    private TextView tvEmptyState;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // Init Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init Views
        recyclerView = findViewById(R.id.recyclerViewMyPosts);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        // Setup RecyclerView
        myPostsList = new ArrayList<>();
        adapter = new MyPostsAdapter(myPostsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load Data
        loadMyPosts();

        // Back Button
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadMyPosts() {
        // 🔥 FIX 1: Use Constants.DB_COMMUNITY_POSTS instead of hardcoded string
        db.collection(Constants.DB_COMMUNITY_POSTS)
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        LogUtils.e("MyPostsActivity", "Listen failed", error);
                        return;
                    }

                    myPostsList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                CommunityPost post = doc.toObject(CommunityPost.class);
                                post.postId = doc.getId();
                                myPostsList.add(post);
                            } catch (Exception e) {
                                LogUtils.e("MyPostsActivity", "Error parsing post", e);
                            }
                        }
                    }

                    // Sort: Newest First
                    myPostsList.sort((p1, p2) -> Long.compare(p2.timestamp, p1.timestamp));

                    adapter.updateList(myPostsList);

                    // Show/Hide Empty State
                    if (myPostsList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // --- DELETE LOGIC ---
    @Override
    public void onDelete(CommunityPost post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // 🔥 FIX 2: Use Constants
                    db.collection(Constants.DB_COMMUNITY_POSTS).document(post.postId)
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- EDIT LOGIC ---
    @Override
    public void onEdit(CommunityPost post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post");

        final EditText input = new EditText(this);

        // 🔥 FIX 3: 'content' ki jagah 'body' use karein (Fetch karte waqt)
        input.setText(post.body);

        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newText)) {

                // 🔥 FIX 4: Update karte waqt field ka naam "body" hona chahiye
                db.collection(Constants.DB_COMMUNITY_POSTS).document(post.postId)
                        .update("body", newText)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Post Updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}