package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    // Views
    private TextView tvFullName, tvEmail, tvProfilePercent, tvLookingFor;
    private TextView tvCgpa, tvBands, tvTargetCountry;
    private ImageView ivProfileImage, btnEditProfile;
    private View btnLogout;
    private LinearLayout btnMyDocuments, btnFavorites;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // 1. Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

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
        btnMyDocuments = findViewById(R.id.btnMyDocuments);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnLogout = findViewById(R.id.btnLogout);

        // 3. Load Data
        loadUserProfile();

        // 4. Setup Button Clicks
        setupClickListeners();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            String uid = user.getUid();

            // 'addValueEventListener' use karein taake Edit hone par foran update ho
            mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // --- FETCH DATA (Keys must match EditProfileActivity) ---
                        String name = snapshot.child("fullName").getValue(String.class);
                        String qual = snapshot.child("qualification").getValue(String.class);

                        // FIXED: 'targetCountry' -> 'targetCountries' (Plural)
                        String country = snapshot.child("targetCountries").getValue(String.class);

                        // Note: EditProfileWizard mein humne 'bands' aur 'cgpa' save nahi kiya tha
                        // Agar future mein add karein to yahan fetch hoga.
                        String bands = snapshot.child("bands").getValue(String.class);
                        String cgpa = snapshot.child("cgpa").getValue(String.class);

                        // --- SET DATA ---
                        tvFullName.setText(name != null ? name : "User");

                        // Country Fix
                        if (country != null && !country.isEmpty()) {
                            // Agar multiple hain to lambi list kharab lagegi, sirf pehli dikhayen ya 'truncate' karein
                            tvTargetCountry.setText(country.length() > 20 ? country.substring(0, 18) + "..." : country);
                        } else {
                            tvTargetCountry.setText("Not Set");
                        }

                        tvBands.setText(bands != null ? bands : "Not Set");
                        tvCgpa.setText(cgpa != null ? cgpa : "Not Set");

                        // Logic: Looking For
                        updateLookingForText(qual);

                        // Logic: Percentage
                        calculateProfileScore(name, qual, country, bands, cgpa);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateLookingForText(String qual) {
        if (qual != null) {
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
        if (name != null) score++;
        if (qual != null) score++;
        if (country != null) score++;
        if (bands != null) score++;
        if (cgpa != null) score++;

        int percentage = (score * 100) / 5;
        tvProfilePercent.setText(percentage + "%");
    }

    private void setupClickListeners() {
        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            // Make sure EditProfileActivity is your WIZARD file
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // My Documents
        btnMyDocuments.setOnClickListener(v -> {
            // Intent intent = new Intent(this, QualificationActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Documents Feature Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Favorites
        btnFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Favorites Feature Coming Soon", Toast.LENGTH_SHORT).show();
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
}