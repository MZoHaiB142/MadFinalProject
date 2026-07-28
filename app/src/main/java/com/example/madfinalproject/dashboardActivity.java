package com.example.madfinalproject;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.example.madfinalproject.adapters.DashboardUniversityAdapter;
import com.example.madfinalproject.adapters.TrendingDestinationAdapter;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.recommendations.TrendingDestinationEngine;
import com.example.madfinalproject.recommendations.UniversityRecommendationEngine;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

public class dashboardActivity extends AppCompatActivity {

    private TextView tvUserName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout btnScholarships, btnCostCalculator, btnSopAssistant,
            btnConsultants, btnRoadmap, btnAiEligible;

    private CardView cvSearchBar, cvScholarshipAlert;
    private ImageView imgBell, imgArrow, imgNotificationBell;
    private TextView tvAlertSubtitle;
    private DashboardUniversityAdapter recommendationAdapter;
    private TrendingDestinationAdapter trendingAdapter;
    private final List<University> recommendationUniversities = new ArrayList<>();
    private DocumentSnapshot recommendationProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // 1. Firebase & Views Initialize
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.tvUserName);
        cvSearchBar = findViewById(R.id.cvsearchBar);
        imgNotificationBell = findViewById(R.id.imgNotificationBell);

        // Alert Card Views
        cvScholarshipAlert = findViewById(R.id.cvScholarshipAlert);
        imgBell = findViewById(R.id.imgBell);
        imgArrow = findViewById(R.id.imgArrow);
        tvAlertSubtitle = findViewById(R.id.tvAlertSubtitle);

        // Grid Buttons (No fake Test button here anymore)
        btnScholarships = findViewById(R.id.btnScholarships);
        btnCostCalculator = findViewById(R.id.btnCostCalculator);
        btnSopAssistant = findViewById(R.id.btnSopAssistant);
        btnConsultants = findViewById(R.id.btnConsultants);
        btnRoadmap = findViewById(R.id.btnRoadmap);
        btnAiEligible = findViewById(R.id.btnAiEligible);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

        // 2. 🔥 Notification Bell Logic
        if (imgNotificationBell != null) {
            startBellPulseAnimation();
            imgNotificationBell.setOnClickListener(v -> {
                try {
                    Intent serviceIntent = new Intent(dashboardActivity.this, MyService.class);
                    stopService(serviceIntent);
                    MyService.stopAlarm();
                    showToast("Alarm Stopped");
                } catch (Exception e) {
                    showToast("No active alarm");
                }
            });
        }

        // 3. Click Listeners
        setupClickListeners();

        // 4. Data Loading & Animations
        loadUserName();
        startAlertAnimations();
        fetchRealTimeScholarshipCount();
        setupChatAccess();
        setupTrendingDestinations();
        setupRealtimeRecommendations();

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔥 Bottom Navigation Logic (Test ID is correctly associated here!)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_explore) {
                startActivity(new Intent(this, ExploreActivity.class));
                return true;
            } else if (itemId == R.id.nav_tests) {
                // Yahan nav_tests seedha TestPreparationActivity ko open karega!
                startActivity(new Intent(this, TestPreparationActivity.class));
                return true;
            } else if (itemId == R.id.nav_community) {
                startActivity(new Intent(this, communitypageActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        if (cvSearchBar != null) {
            cvSearchBar.setOnClickListener(v -> startActivity(new Intent(this, ExploreActivity.class)));
        }
        if (btnScholarships != null) btnScholarships.setOnClickListener(v -> startActivity(new Intent(this, ScholarshipActivity.class)));
        if (btnCostCalculator != null) btnCostCalculator.setOnClickListener(v -> startActivity(new Intent(this, CostCalculatorActivity.class)));
        if (cvScholarshipAlert != null) cvScholarshipAlert.setOnClickListener(v -> startActivity(new Intent(this, ScholarshipActivity.class)));
        if (btnSopAssistant != null) btnSopAssistant.setOnClickListener(v -> startActivity(new Intent(this, CountrySelectionActivity.class)));
        if (btnRoadmap != null) btnRoadmap.setOnClickListener(v -> startActivity(new Intent(this,CountrySelectionRoadmapActivity.class)));
        if (btnAiEligible != null) btnAiEligible.setOnClickListener(v -> startActivity(new Intent(this, AIEligibilityActivity.class)));
        if (btnConsultants != null) btnConsultants.setOnClickListener(v -> startActivity(new Intent(this, LiveConsultantActivity.class)));
        View seeAll = findViewById(R.id.btnSeeAllRecommended);
        if (seeAll != null) seeAll.setOnClickListener(v -> startActivity(new Intent(this, ExploreActivity.class)));
        View viewAllTrending = findViewById(R.id.btnViewAllTrending);
        if (viewAllTrending != null) viewAllTrending.setOnClickListener(
                v -> startActivity(new Intent(this, ExploreActivity.class)));
    }

    private void setupTrendingDestinations() {
        RecyclerView trending = findViewById(R.id.rv_trending);
        trending.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        trendingAdapter = new TrendingDestinationAdapter(this);
        trending.setAdapter(trendingAdapter);
    }

    private void setupRealtimeRecommendations() {
        RecyclerView recommendations = findViewById(R.id.rv_recommended);
        recommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationAdapter = new DashboardUniversityAdapter(this);
        recommendations.setAdapter(recommendationAdapter);

        db.collection("Universities").addSnapshotListener(this, (snapshot, error) -> {
            if (error != null) { LogUtils.e("Dashboard", "University recommendations failed", error); return; }
            recommendationUniversities.clear();
            if (snapshot != null) for (DocumentSnapshot document : snapshot.getDocuments()) {
                try {
                    University university = document.toObject(University.class);
                    if (university != null) { university.id = document.getId(); recommendationUniversities.add(university); }
                } catch (RuntimeException parseError) {
                    LogUtils.e("Dashboard", "Skipping malformed university", parseError);
                }
            }
            refreshRecommendations();
            refreshTrendingDestinations();
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) db.collection(Constants.DB_USERS).document(user.getUid())
                .addSnapshotListener(this, (profile, error) -> {
                    if (error != null) { LogUtils.e("Dashboard", "Profile recommendation listener failed", error); return; }
                    recommendationProfile = profile;
                    refreshRecommendations();
                });
    }

    private void refreshRecommendations() {
        if (recommendationAdapter == null || recommendationUniversities.isEmpty()) return;
        recommendationAdapter.submit(UniversityRecommendationEngine.rank(
                recommendationUniversities, recommendationProfile));
    }

    private void refreshTrendingDestinations() {
        if (trendingAdapter == null) return;
        trendingAdapter.submit(TrendingDestinationEngine.rank(
                recommendationUniversities, 5));
    }

    private void startBellPulseAnimation() {
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(imgNotificationBell, "scaleX", 1f, 1.2f, 1f);
        pulseX.setDuration(1500);
        pulseX.setRepeatCount(ObjectAnimator.INFINITE);
        pulseX.start();

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(imgNotificationBell, "scaleY", 1f, 1.2f, 1f);
        pulseY.setDuration(1500);
        pulseY.setRepeatCount(ObjectAnimator.INFINITE);
        pulseY.start();
    }

    private void startAlertAnimations() {
        if (imgBell != null) {
            ObjectAnimator bellAnim = ObjectAnimator.ofFloat(imgBell, "rotation", 0f, 20f, -20f, 0f);
            bellAnim.setDuration(1200);
            bellAnim.setRepeatCount(ObjectAnimator.INFINITE);
            bellAnim.start();
        }
        if (imgArrow != null) {
            ObjectAnimator arrowAnim = ObjectAnimator.ofFloat(imgArrow, "translationX", 0f, 15f);
            arrowAnim.setDuration(800);
            arrowAnim.setRepeatCount(ObjectAnimator.INFINITE);
            arrowAnim.setRepeatMode(ObjectAnimator.REVERSE);
            arrowAnim.start();
        }
    }

    private void fetchRealTimeScholarshipCount() {
        db.collectionGroup("data").addSnapshotListener((value, error) -> {
            if (value != null) {
                int count = value.size();
                tvAlertSubtitle.setText(count > 0 ? count + " scholarships match your profile" : "Checking for updates...");
            }
        });
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString(Constants.KEY_FULL_NAME);
                            tvUserName.setText("Welcome back, " + (name != null ? name : "User") + "! 👋");
                        }
                    });
        }
    }

    private void setupChatAccess() {
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        if (fabChat != null) {
            fabChat.setOnClickListener(v ->
                    startActivity(
                            new Intent(this, ChatActivity.class))
            );
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
