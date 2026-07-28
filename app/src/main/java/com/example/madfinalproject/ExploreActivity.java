package com.example.madfinalproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.ExploreAdapter;
import com.example.madfinalproject.engine.AiEligibilityEngine;
import com.example.madfinalproject.engine.AiResultParser;
import com.example.madfinalproject.models.AiUniversityResult;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.models.UniversityMatch;
import com.example.madfinalproject.models.UniversityProfileMatch;
import com.example.madfinalproject.models.UserProfile;
import com.example.madfinalproject.recommendations.UniversityConsultantMatchEngine;
import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExploreAdapter adapter;
    private List<University> allUniList = new ArrayList<>();
    private List<University> filteredList = new ArrayList<>();
    private Map<String, UniversityProfileMatch> profileMatches = new LinkedHashMap<>();
    private DocumentSnapshot profileDocument;
    private String activeCategory = "all";
    private String lastAiSignature = "";

    private EditText etSearch;
    private TextView filterAll, filterSafe, filterTarget, filterAmbitious;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 502);
        }

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Init Views
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerViewUniversities);

        String countryFilter = getIntent().getStringExtra("country_filter");
        if (countryFilter != null && !countryFilter.trim().isEmpty()) {
            etSearch.setText(countryFilter.trim());
        }

        filterAll = findViewById(R.id.filterAll);
        filterSafe = findViewById(R.id.filterSafe);
        filterTarget = findViewById(R.id.filterTarget);
        filterAmbitious = findViewById(R.id.filterAmbitious);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExploreAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // Load Data
        loadUserProfile();
        loadUniversities();

        // Search Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Buttons (Placeholder Logic)
        setupFilters();
    }

    private void loadUniversities() {
        // 🔥 Firestore Collection Name: "Universities"
        db.collection("Universities")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            LogUtils.e("ExploreActivity", "Listen failed.", error);
                            return;
                        }

                        allUniList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                try {
                                    University uni = doc.toObject(University.class);
                                    uni.id = doc.getId();
                                    allUniList.add(uni);
                                } catch (Exception e) {
                                    LogUtils.e("ExploreActivity", "Error parsing university", e);
                                }
                            }
                        }
                        recalculateProfileMatches();
                    }
                });
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            profileDocument = null;
            recalculateProfileMatches();
            return;
        }

        db.collection(Constants.DB_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    profileDocument = document.exists() ? document : null;
                    recalculateProfileMatches();
                })
                .addOnFailureListener(error -> {
                    LogUtils.e("ExploreActivity", "Profile match data failed.", error);
                    profileDocument = null;
                    recalculateProfileMatches();
                });
    }

    private void recalculateProfileMatches() {
        profileMatches = UniversityConsultantMatchEngine.analyze(
                allUniList,
                profileDocument
        );
        adapter.updateProfileMatches(profileMatches);
        applyFilters();
        runAiRefinement();
    }

    private void applyFilters() {
        List<University> temp = new ArrayList<>();
        String query = etSearch == null
                ? ""
                : etSearch.getText().toString().trim().toLowerCase(Locale.US);
        for (University uni : allUniList) {
            String name = uni.name == null ? "" : uni.name.toLowerCase();
            String location = uni.location == null ? "" : uni.location.toLowerCase();
            boolean searchMatches = query.isEmpty()
                    || name.contains(query)
                    || location.contains(query);
            if (!searchMatches) continue;

            UniversityProfileMatch match = profileMatches.get(
                    UniversityConsultantMatchEngine.key(uni)
            );
            boolean categoryMatches = "all".equals(activeCategory)
                    || (match != null && activeCategory.equals(match.getCategory()));
            if (categoryMatches) temp.add(uni);
        }
        adapter.updateList(temp);
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> {
            activeCategory = "all";
            applyFilters();
            Toast.makeText(this, "Showing All", Toast.LENGTH_SHORT).show();
        });

        filterSafe.setOnClickListener(v -> {
            activeCategory = "Safe";
            applyFilters();
            Toast.makeText(this, "Showing highly suitable universities", Toast.LENGTH_SHORT).show();
        });
        filterTarget.setOnClickListener(v -> {
            activeCategory = "Target";
            applyFilters();
            Toast.makeText(this, "Showing suitable target universities", Toast.LENGTH_SHORT).show();
        });
        filterAmbitious.setOnClickListener(v -> {
            activeCategory = "Ambitious";
            applyFilters();
            Toast.makeText(this, "Showing ambitious universities", Toast.LENGTH_SHORT).show();
        });
    }

    private void runAiRefinement() {
        if (profileDocument == null
                || !profileDocument.exists()
                || allUniList.isEmpty()) {
            return;
        }

        String signature = buildAiSignature();
        if (signature.equals(lastAiSignature)) return;
        lastAiSignature = signature;

        UserProfile profile = buildAiProfile(profileDocument);
        List<UniversityMatch> candidates = buildAiCandidates();
        if (candidates.isEmpty()) return;

        AiEligibilityEngine.analyzeEligibility(
                profile,
                candidates,
                new AiEligibilityEngine.AiResponseCallback() {
                    @Override
                    public void onSuccess(String aiResponse) {
                        List<AiUniversityResult> aiResults =
                                AiResultParser.parse(aiResponse);
                        if (aiResults.isEmpty()) {
                            LogUtils.d("ExploreActivity",
                                    "AI returned no refinements; consultant matches retained.");
                            return;
                        }

                        runOnUiThread(() -> {
                            if (isFinishing()
                                    || (Build.VERSION.SDK_INT >= 17 && isDestroyed())) {
                                return;
                            }
                            applyAiRefinements(aiResults);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        LogUtils.d(
                                "ExploreActivity",
                                "AI refinement unavailable; consultant matches retained: "
                                        + errorMessage
                        );
                    }
                }
        );
    }

    private void applyAiRefinements(List<AiUniversityResult> aiResults) {
        for (AiUniversityResult ai : aiResults) {
            University university = findUniversity(ai.getUniversityName());
            if (university == null) continue;

            String key = UniversityConsultantMatchEngine.key(university);
            UniversityProfileMatch local = profileMatches.get(key);
            if (local == null) continue;

            UniversityProfileMatch refined =
                    UniversityConsultantMatchEngine.refineWithAi(local, ai);
            profileMatches.put(key, refined);
            university.setMatchScore(refined.getScore());
        }
        adapter.updateProfileMatches(profileMatches);
        applyFilters();
    }

    private University findUniversity(String universityName) {
        String target = normalizeName(universityName);
        if (target.isEmpty()) return null;
        for (University university : allUniList) {
            String candidate = normalizeName(university.name);
            if (candidate.equals(target)
                    || candidate.contains(target)
                    || target.contains(candidate)) {
                return university;
            }
        }
        return null;
    }

    private UserProfile buildAiProfile(DocumentSnapshot document) {
        UserProfile profile = new UserProfile();
        profile.setName(profileValue(document, Constants.KEY_FULL_NAME));
        profile.setGpa(profileNumber(document, Constants.KEY_CGPA, "gpa"));
        profile.setIelts(profileNumber(
                document,
                Constants.KEY_IELTS_SCORE,
                "ielts",
                "pte"
        ));
        profile.setDegreeLevel(profileValue(
                document,
                Constants.KEY_QUALIFICATION,
                Constants.KEY_DEGREE_LEVEL
        ));
        profile.setField(profileValue(
                document,
                Constants.KEY_INTERESTED_FIELDS,
                "field"
        ));
        profile.setBudget(profileValue(document, Constants.KEY_BUDGET, "budget"));
        profile.setPreferredCountry(profileValue(
                document,
                Constants.KEY_TARGET_COUNTRIES,
                "country"
        ));
        profile.setExperienceYears((int) profileNumber(
                document,
                "experience_years",
                "experienceYears"
        ));
        return profile;
    }

    private List<UniversityMatch> buildAiCandidates() {
        List<UniversityMatch> candidates = new ArrayList<>();
        for (University university : allUniList) {
            if (candidates.size() >= 8) break;
            UniversityMatch candidate = new UniversityMatch();
            candidate.setId(university.id);
            candidate.setName(university.name == null ? "" : university.name);
            candidate.setCountry(university.location == null ? "" : university.location);
            candidate.setCountryName(university.location == null ? "" : university.location);
            candidate.setFees(university.fees == null ? "" : university.fees);
            candidate.setAcceptanceRate(percent(university.acceptanceRate));
            candidate.setVisaRatePakistan(percent(university.visaRatio));
            candidate.setScholarshipAvailable(
                    university.scholarshipCount > 0
                            || !university.getScholarships().isEmpty()
            );

            if (!university.getPrograms().isEmpty()) {
                University.Program program = university.getPrograms().get(0);
                candidate.setProgram(program.getCourseName());
                candidate.setLevel(program.getDegreeLevel());
                if (!program.getYearlyFees().isEmpty()) {
                    candidate.setFees(program.getYearlyFees());
                }
                candidate.setDuration(program.getDuration());
            }
            candidates.add(candidate);
        }
        return candidates;
    }

    private String buildAiSignature() {
        StringBuilder value = new StringBuilder();
        if (profileDocument.getData() != null) {
            value.append(profileDocument.getData().hashCode());
        }
        for (University university : allUniList) {
            value.append('|')
                    .append(university.id)
                    .append(':')
                    .append(university.name)
                    .append(':')
                    .append(university.acceptanceRate)
                    .append(':')
                    .append(university.visaRatio)
                    .append(':')
                    .append(university.getPrograms().size());
        }
        return String.valueOf(value.toString().hashCode());
    }

    private String profileValue(DocumentSnapshot document, String... keys) {
        if (document == null) return "";
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private double profileNumber(DocumentSnapshot document, String... keys) {
        String value = profileValue(document, keys);
        if (value.isEmpty()) return 0;
        String clean = value.replaceAll("[^0-9.]", "");
        try {
            return clean.isEmpty() ? 0 : Double.parseDouble(clean);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int percent(String value) {
        if (value == null) return 0;
        String clean = value.replaceAll("[^0-9.]", "");
        try {
            return clean.isEmpty() ? 0 : Math.round(Float.parseFloat(clean));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String normalizeName(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]", "");
    }

    public void setDeadlineReminder(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month); // Month 0-11 hota hai
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 9); // Subah 9 baje alert
        calendar.set(Calendar.MINUTE, 0);

        Intent intent = new Intent(this, MyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Deadline Reminder Set!", Toast.LENGTH_SHORT).show();
    }
}
