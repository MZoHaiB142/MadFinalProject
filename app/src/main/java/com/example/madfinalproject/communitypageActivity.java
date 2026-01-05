package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class communitypageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<CommunityPost> allPosts = new ArrayList<>();
    private ChipGroup chipGroup;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_page);

        // 1. Setup Views
        recyclerView = findViewById(R.id.recyclerViewPosts);
        chipGroup = findViewById(R.id.chipGroupFilters);
        Button btnAsk = findViewById(R.id.btnAskQuestion);

        // 2. Dummy Data (Bilkul Image jaisa)
        populateData();

        // 3. Setup RecyclerView
        adapter = new CommunityAdapter(allPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 4. Filter Logic
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipGeneral) {
                filterList("General");
            } else if (checkedId == R.id.chipVisa) {
                filterList("Visa Help");
            } else if (checkedId == R.id.chipIelts) {
                filterList("IELTS Prep");
            } else if (checkedId == R.id.chipUni) {
                filterList("University Life");
            } else {
                // Agar kuch select na ho to sab dikhao
                adapter.updateList(allPosts);
            }
        });

        // Default Selection
        chipGroup.check(R.id.chipGeneral);

        // 5. Ask Question Button
        btnAsk.setOnClickListener(v -> {
            // Yahan Ask Question Activity khulni chahiye
            Toast.makeText(this, "Open Ask Question Page", Toast.LENGTH_SHORT).show();
        });
        // 1. Setup Views
        recyclerView = findViewById(R.id.recyclerViewPosts);
        chipGroup = findViewById(R.id.chipGroupFilters);
         btnAsk = findViewById(R.id.btnAskQuestion);

        // --- BOTTOM NAVIGATION SETUP START ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

        // ✅ Step A: Highlight the correct icon (Community)
        bottomNavigationView.setSelectedItemId(R.id.nav_community);

        // ✅ Step B: Navigation Logic
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(communitypageActivity.this, dashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Animation remove karein (Static feel ke liye)
                finish(); // Close current activity
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
                return true; // Already here
            }
            return false;
        });
        // --- BOTTOM NAVIGATION SETUP END ---
    }

    private void filterList(String category) {
        if (category.equals("General")) {
            adapter.updateList(allPosts); // Show All
            return;
        }

        List<CommunityPost> filtered = new ArrayList<>();
        for (CommunityPost post : allPosts) {
            if (post.category.equals(category)) {
                filtered.add(post);
            }
        }
        adapter.updateList(filtered);
    }

    private void populateData() {
        // Image Data
        allPosts.add(new CommunityPost("Ali Khan", "2h ago", "Student", "Visa interview tips for F1?", "My interview is next week. Any specific questions I should prepare for regarding funding?", "Visa Help", 34, 12));
        allPosts.add(new CommunityPost("Sarah Ahmed", "5h ago", "Student", "IELTS Speaking: How to score 8.0?", "I'm stuck at 7.0. Need advice on lexical resource improvements.", "IELTS Prep", 21, 8));

        // Extra Data
        allPosts.add(new CommunityPost("Usman", "1d ago", "Alumni", "Best universities in Germany?", "Looking for low tuition fee options for CS.", "University Life", 50, 40));
        allPosts.add(new CommunityPost("Ayesha", "30m ago", "Student", "Is bank statement mandatory for offer letter?", "Applying to UK universities.", "Visa Help", 10, 2));
    }
}