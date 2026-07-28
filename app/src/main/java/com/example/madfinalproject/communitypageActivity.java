package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
// ✅ Firestore Imports
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class communitypageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<CommunityPost> allPosts = new ArrayList<>();
    private List<CommunityPost> filteredPosts = new ArrayList<>();
    private ChipGroup chipGroup;
    private String currentFilter = Constants.CATEGORY_GENERAL;

    private FirebaseAuth mAuth;
    // ✅ Firestore Reference
    private FirebaseFirestore db;
    // ✅ Listener Registration (to remove later)
    private ListenerRegistration postsListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_page);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        // ✅ Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // 1. Setup Views
        recyclerView = findViewById(R.id.recyclerViewPosts);
        chipGroup = findViewById(R.id.chipGroupFilters);
        Button btnAsk = findViewById(R.id.btnAskQuestion);

        // 2. Setup RecyclerView
        adapter = new CommunityAdapter(new ArrayList<>(), this, mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 3. Load Posts from Firestore (Real-time)
        loadPosts();

        // 4. Filter Logic
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipGeneral) {
                currentFilter = Constants.CATEGORY_GENERAL;
                filterList(Constants.CATEGORY_GENERAL);
            } else if (checkedId == R.id.chipVisa) {
                currentFilter = Constants.CATEGORY_VISA_HELP;
                filterList(Constants.CATEGORY_VISA_HELP);
            } else if (checkedId == R.id.chipIelts) {
                currentFilter = Constants.CATEGORY_IELTS_PREP;
                filterList(Constants.CATEGORY_IELTS_PREP);
            } else if (checkedId == R.id.chipUni) {
                currentFilter = Constants.CATEGORY_UNIVERSITY_LIFE;
                filterList(Constants.CATEGORY_UNIVERSITY_LIFE);
            } else {
                currentFilter = Constants.CATEGORY_GENERAL;
                filterList(Constants.CATEGORY_GENERAL);
            }
        });

        // Default Selection
        chipGroup.check(R.id.chipGeneral);

        // 5. Ask Question Button
        btnAsk.setOnClickListener(v -> {
            Intent intent = new Intent(communitypageActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        // --- BOTTOM NAVIGATION SETUP ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_community);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(communitypageActivity.this, dashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(communitypageActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_community) {
                return true;
            }
            return false;
        });
    }

    private void loadPosts() {
        // Remove previous listener if exists
        if (postsListener != null) {
            postsListener.remove();
        }

        // ✅ Firestore Query: Collection "community_posts" ordered by "timestamp" descending
        Query query = db.collection(Constants.DB_COMMUNITY_POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        postsListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    LogUtils.e("CommunityActivity", "Listen failed.", error);
                    Toast.makeText(communitypageActivity.this, "Error loading posts", Toast.LENGTH_SHORT).show();
                    return;
                }

                allPosts.clear();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        // ✅ Convert Document to Object
                        CommunityPost post = doc.toObject(CommunityPost.class);
                        post.postId = doc.getId(); // Firestore ID manually set karein

                        // Handle Likes (Map)
                        // Firestore automatically maps it if field name matches
                        if (post.likes == null) {
                            post.likes = new java.util.HashMap<>();
                        }

                        // Check if current user liked this post
                        if (mAuth.getCurrentUser() != null) {
                            post.isLiked = post.likes.containsKey(mAuth.getCurrentUser().getUid());
                        }

                        allPosts.add(post);
                    } catch (Exception e) {
                        LogUtils.e("CommunityActivity", "Error parsing post", e);
                    }
                }

                // Firestore query already sorted, but local filter logic needs list
                filterList(currentFilter);
                LogUtils.d("CommunityActivity", "Loaded " + allPosts.size() + " posts");
            }
        });
    }

    private void filterList(String category) {
        filteredPosts.clear();

        if (category.equals(Constants.CATEGORY_GENERAL)) {
            filteredPosts.addAll(allPosts);
        } else {
            for (CommunityPost post : allPosts) {
                if (post.category != null && post.category.equals(category)) {
                    filteredPosts.add(post);
                }
            }
        }
        adapter.updateList(filteredPosts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (postsListener != null) {
            postsListener.remove();
        }
    }
}