package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Image loading ke liye
import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends AppCompatActivity {

    // Views
    private TextView tvFullName, tvEmail, tvProfilePercent, tvLookingFor;
    private TextView tvCgpa, tvBands, tvTargetCountry;
    private ImageView ivProfileImage, btnEditProfile;
    private View btnLogout;

    // Updated Buttons
    private LinearLayout btnMyDocuments, btnMyPosts, btnSettings;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Listener
    private ListenerRegistration userProfileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // 1. Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Init Views
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvProfilePercent = findViewById(R.id.tvProfilePercent);
        tvLookingFor = findViewById(R.id.tvLookingFor);
        tvCgpa = findViewById(R.id.tvCgpa);
        tvBands = findViewById(R.id.tvBands);
        tvTargetCountry = findViewById(R.id.tvTargetCountry);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Button IDs Match with XML
        btnMyDocuments = findViewById(R.id.btnMyDocuments);
        btnMyPosts = findViewById(R.id.btnMyPosts);
        btnSettings = findViewById(R.id.btnSettings);

        // 3. Load Data
        loadUserProfile();

        // 4. Setup Clicks
        setupClickListeners();

        // 5. Bottom Navigation
        setupBottomNavigation();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            String uid = user.getUid();

            userProfileListener = db.collection("Users").document(uid)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                LogUtils.e("ProfileActivity", "Listen failed.", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                try {
                                    // Fetch Data
                                    String name = snapshot.getString(Constants.KEY_FULL_NAME);
                                    if (name == null) name = "User";

                                    String qual = snapshot.getString(Constants.KEY_QUALIFICATION);
                                    String country = snapshot.getString(Constants.KEY_TARGET_COUNTRIES);
                                    String profileImage = snapshot.getString(Constants.KEY_PROFILE_IMAGE);

                                    // 🔥 PERFECT DP LOGIC HERE
                                    if (profileImage != null && !profileImage.isEmpty()) {
                                        Glide.with(ProfileActivity.this)
                                                .load(profileImage)
                                                .placeholder(R.drawable.user_profile) // Load hote hue dummy
                                                .error(R.drawable.user_profile)       // Error aaye toh dummy
                                                .circleCrop()                         // Perfect gol (round) image
                                                .into(ivProfileImage);
                                    } else {
                                        // Agar URL na ho toh direct dummy
                                        Glide.with(ProfileActivity.this)
                                                .load(R.drawable.user_profile)
                                                .circleCrop()
                                                .into(ivProfileImage);
                                    }

                                    // Bands & CGPA Logic
                                    String bands = null;
                                    if (snapshot.contains("bands")) bands = String.valueOf(snapshot.get("bands"));

                                    String cgpa = null;
                                    if (snapshot.contains("cgpa")) cgpa = String.valueOf(snapshot.get("cgpa"));
                                    else if (snapshot.contains(Constants.KEY_CGPA)) cgpa = String.valueOf(snapshot.get(Constants.KEY_CGPA));

                                    // Set Text
                                    tvFullName.setText(name);
                                    tvTargetCountry.setText((country != null && !country.isEmpty()) ? country : "Not Set");
                                    tvBands.setText((bands != null && !bands.isEmpty()) ? bands : "Not Set");
                                    tvCgpa.setText((cgpa != null && !cgpa.isEmpty()) ? cgpa : "Not Set");

                                    updateLookingForText(qual);
                                    calculateProfileScore(name, qual, country, bands, cgpa);

                                } catch (Exception ex) {
                                    LogUtils.e("ProfileActivity", "Error parsing profile", ex);
                                }
                            }
                        }
                    });
        }
    }

    private void updateLookingForText(String qual) {
        if (qual != null && !qual.equals("null")) {
            String q = qual.toLowerCase();
            if (q.contains("bachelor") || q.contains("bs")) {
                tvLookingFor.setText("Master's Degree");
            } else if (q.contains("inter") || q.contains("college") || q.contains("level")) {
                tvLookingFor.setText("Bachelor's Degree");
            } else if (q.contains("master") || q.contains("ms")) {
                tvLookingFor.setText("PhD / Research");
            } else {
                tvLookingFor.setText("Higher Education");
            }
        } else {
            tvLookingFor.setText("---");
        }
    }

    private void calculateProfileScore(String name, String qual, String country, String bands, String cgpa) {
        int score = 0;
        if (name != null && !name.equals("User")) score++;
        if (qual != null && !qual.equals("null")) score++;
        if (country != null && !country.equals("null")) score++;
        if (bands != null && !bands.equals("null")) score++;
        if (cgpa != null && !cgpa.equals("null")) score++;

        int percentage = (score * 100) / 5;
        tvProfilePercent.setText(percentage + "%");
    }

    private void setupClickListeners() {
        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // My Documents
        btnMyDocuments.setOnClickListener(v -> {
            Toast.makeText(this, "My Documents Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // My Posts
        btnMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
            startActivity(intent);
        });

        // Settings
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, loginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setItemIconTintList(null);
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, dashboardActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                } else if (itemId == R.id.nav_community) {
                    startActivity(new Intent(ProfileActivity.this, communitypageActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userProfileListener != null) {
            userProfileListener.remove();
        }
    }
}