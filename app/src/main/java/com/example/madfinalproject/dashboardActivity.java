package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class dashboardActivity extends AppCompatActivity {

    // Variables declare karein
    private TextView tvUserName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // 1. Views Initialize karein
        tvUserName = findViewById(R.id.tvUserName); // XML wali ID
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

        // 2. Firebase Initialize karein
        mAuth = FirebaseAuth.getInstance();
        // "Users" wahi folder hai jahan humne signup data save kiya tha
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // 3. User ka naam load karne wala function chalayein
        loadUserName();

        // 4. Window Insets (Apka purana design code)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // ====================================================
        // ✅ 5. BOTTOM NAVIGATION LISTENER PROFILE
        // ====================================================
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Agar user "Profile" icon par click kare
            if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(dashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            // Future mein Home ya doosre buttons ke liye yahan code aayega
            // else if (itemId == R.id.nav_home) { ... }

            return false;
        });
        // ====================================================
        // ✅ 5. BOTTOM NAVIGATION LISTENER COMMUNITY
        // ====================================================
        // ... Upar wala code waisa hi rahega ...

// Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // 1. Profile Page
            if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(dashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            // ✅ 2. Community Page (YE ADD KAREIN)
            else if (itemId == R.id.nav_community) {
                // "nav_community" aapke menu xml ki ID honi chahiye
                Intent intent = new Intent(dashboardActivity.this, communitypageActivity.class);
                startActivity(intent);
                return true;
            }

            // 3. Home / Dashboard (Optional: Refresh karne ke liye)
            else if (itemId == R.id.nav_home) {
                // Already here, kuch karne ki zaroorat nahi ya refresh logic lagayein
                return true;
            }

            return false;
        });

    }

    // --- DATA FETCHING LOGIC ---
    private void loadUserName() {
        // Abhi jo user login hai, usay get karein
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid(); // User ki unique ID

            // Database mein us ID par jayein
            mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check karein ke data mil gaya ya nahi
                    if (snapshot.exists()) {
                        // "fullName" key se value nikalein (Signup ke waqt yehi key use ki thi)
                        String fullName = snapshot.child("fullName").getValue(String.class);

                        if (fullName != null) {
                            // TextView par naam set karein
                            tvUserName.setText("Welcome back, " + fullName + "! 👋");
                        }
                    } else {
                        // Agar data na mile
                        Toast.makeText(dashboardActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Agar koi network error aaye
                    Toast.makeText(dashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}