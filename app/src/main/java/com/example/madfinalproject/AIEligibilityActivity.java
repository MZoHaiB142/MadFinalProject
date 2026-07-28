package com.example.madfinalproject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.madfinalproject.adapters.AiResultAdapter;
import com.example.madfinalproject.databinding.ActivityAiEligibilityBinding;
import com.example.madfinalproject.engine.AiEligibilityEngine;
import com.example.madfinalproject.engine.AiResultParser;
import com.example.madfinalproject.engine.EligibilityScoringEngine;
import com.example.madfinalproject.models.AiUniversityResult;
import com.example.madfinalproject.models.UniversityMatch;
import com.example.madfinalproject.models.UserProfile;
import com.example.madfinalproject.utils.Constants; // 🔥 NAYA IMPORT ADD KIYA HAI
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIEligibilityActivity extends AppCompatActivity {

    private static final String TAG = "AIEligibilityActivity";

    private ActivityAiEligibilityBinding binding;
    private FirebaseFirestore  db;
    private FirebaseAuth  auth;

    private List<AiUniversityResult> allResults   = new ArrayList<>();
    private List<UniversityMatch>    universities  = new ArrayList<>();
    private UserProfile              userProfile;
    private AiResultAdapter          adapter;

    private String activeFilter = "all";
    private String sortMode     = "score";
    private int    sortIdx      = 0;
    private final String[] sortCycle = {"score", "priority", "name"};
    private final String[] sortLabel = {"AI Score", "Apply Priority", "Name A-Z"};

    // Analyze messages
    private final String[] analyzeMessages = {
            "Reading your academic profile...",
            "Checking GPA, English score, and budget...",
            "Matching your profile with universities...",
            "Calculating visa and acceptance indicators...",
            "Estimating scholarship opportunities...",
            "Preparing consultant-style recommendations...",
            "Sorting your eligibility results..."
    };
    private int      msgIdx = 0;
    private Handler  handler;
    private Runnable msgRunnable;
    private int analysisGeneration = 0;

    // ─────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiEligibilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupSearch();
        setupSort();
        setupFilters();
        setupButtons();
        loadDataAndRunAI();
    }

    // ─────────────────────────────────────────────
    // SETUP
    // ─────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new AiResultAdapter(result -> showDetailSheet(result));
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(adapter);
        binding.rvMatches.setNestedScrollingEnabled(false);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) { applyFilterAndSort(); }
        });
    }

    private void setupSort() {
        binding.btnSort.setOnClickListener(v -> {
            sortIdx = (sortIdx + 1) % sortCycle.length;
            sortMode = sortCycle[sortIdx];
            binding.btnSort.setText("Sort: " + sortLabel[sortIdx]);
            applyFilterAndSort();
        });
    }

    private void setupButtons() {
        binding.btnReanalyze.setOnClickListener(v -> {
            if (userProfile != null && !universities.isEmpty()) {
                runAiAnalysis();
            } else {
                loadDataAndRunAI();
            }
        });
    }

    private void setupFilters() {
        String[][] filters = {
                {"all",        "All Results"},
                {"safe",       "Safe"},
                {"target",     "Target"},
                {"ambitious",  "Ambitious"},
                {"high_schol", "High Scholarship"},
                {"high_visa",  "Good Visa"},
        };

        binding.filterChipsContainer.removeAllViews();

        for (String[] f : filters) {
            String key   = f[0];
            String label = f[1];

            LinearLayout chip = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.item_filter_chip,
                            binding.filterChipsContainer, false);

            TextView tvChip = chip.findViewById(R.id.tv_chip);
            tvChip.setText(label);

            chip.setBackgroundResource(R.drawable.bg_chip_default);
            tvChip.setTextColor(
                    ContextCompat.getColor(this, R.color.text_secondary));

            if (key.equals("all")) {
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                tvChip.setTextColor(
                        ContextCompat.getColor(this, R.color.blue_light));
            }

            chip.setOnClickListener(v -> {
                // Reset all
                for (int i = 0;
                     i < binding.filterChipsContainer.getChildCount(); i++) {
                    View c = binding.filterChipsContainer.getChildAt(i);
                    c.setBackgroundResource(R.drawable.bg_chip_default);
                    TextView tv = c.findViewById(R.id.tv_chip);
                    if (tv != null) tv.setTextColor(
                            ContextCompat.getColor(this, R.color.text_secondary));
                }
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                tvChip.setTextColor(
                        ContextCompat.getColor(this, R.color.blue_light));

                activeFilter = key;
                applyFilterAndSort();
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);
            binding.filterChipsContainer.addView(chip);
        }
    }

    // ─────────────────────────────────────────────
    // FIREBASE LOAD → AI CALL
    // ─────────────────────────────────────────────

    private void loadDataAndRunAI() {
        showAnalyzing(true);
        startMsgAnimation();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            stopMsgAnimation();
            showAnalyzing(false);
            Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 FIXED: "users" ko "Users" kar diya gaya hai (Capital U)
        db.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    // Agar user ka document hi nahi hai
                    if (!doc.exists()) {
                        stopMsgAnimation();
                        showAnalyzing(false);
                        showProfileIncompleteDialog();
                        return;
                    }

                    userProfile = buildProfile(doc);

                    // Check profile completion
                    if (isProfileIncomplete(userProfile)) {
                        stopMsgAnimation();
                        showAnalyzing(false);
                        showProfileIncompleteDialog();
                        return;
                    }

                    updateProfileUI(userProfile);

                    // Step 2 — Universities load
                    db.collection("Universities")
                            .get()
                            .addOnSuccessListener(snap -> {
                                universities = parseUniversities(snap);
                                if (universities.isEmpty()) {
                                    stopMsgAnimation();
                                    showAnalyzing(false);
                                    showError("No universities or programs are available.");
                                    return;
                                }
                                // Step 3 — AI call
                                runAiAnalysis();
                            })
                            .addOnFailureListener(e -> {
                                stopMsgAnimation();
                                showAnalyzing(false);
                                showError("Unable to load universities: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    stopMsgAnimation();
                    showAnalyzing(false);
                    showError("Unable to load your profile: " + e.getMessage());
                });
    }

    // 🔥 FIXED: Sirf Name, GPA, aur Degree Level check karega (IELTS ka masla khatam)
    private boolean isProfileIncomplete(UserProfile p) {
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            Toast.makeText(this, "Your full name is missing.", Toast.LENGTH_LONG).show();
            return true;
        }
        if (p.getGpa() <= 0.0) {
            Toast.makeText(this, "Your GPA is missing or invalid.", Toast.LENGTH_LONG).show();
            return true;
        }
        if (p.getDegreeLevel() == null || p.getDegreeLevel().trim().isEmpty()) {
            Toast.makeText(this, "Your qualification is missing.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void showProfileIncompleteDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Incomplete Profile")
                .setMessage("Your name and GPA are required for AI analysis. Please complete your profile first.")
                .setCancelable(false)
                .setPositiveButton("Go Back", (dialog, which) -> {
                    finish();
                })
                .show();
    }

    // ─────────────────────────────────────────────
    // AI ENGINE CALL
    // ─────────────────────────────────────────────

    private void runAiAnalysis() {
        showAnalyzing(true);
        if (!isAnimating()) startMsgAnimation();

        final int requestId = ++analysisGeneration;
        final List<AiUniversityResult> guaranteedResults =
                EligibilityScoringEngine.analyze(userProfile, universities);

        if (guaranteedResults.isEmpty()) {
            stopMsgAnimation();
            showAnalyzing(false);
            showError("No valid universities are available for eligibility analysis.");
            return;
        }

        // Render a complete result immediately. Remote AI only refines it.
        showResults(
                guaranteedResults,
                "Instant eligibility results are ready. AI refinement is running in the background."
        );

        AiEligibilityEngine.analyzeEligibility(
                userProfile,
                universities,
                new AiEligibilityEngine.AiResponseCallback() {

                    @Override
                    public void onSuccess(String aiResponse) {
                        List<AiUniversityResult> aiResults = AiResultParser.parse(aiResponse);

                        runOnUiThread(() -> {
                            if (requestId != analysisGeneration || binding == null) return;

                            if (aiResults.isEmpty()) {
                                binding.tvAiInsight.setText(
                                        buildInsightSummary(
                                                guaranteedResults,
                                                "Smart weighted analysis"
                                        )
                                );
                                Log.w(TAG, "AI response was empty; guaranteed results retained.");
                                return;
                            }

                            List<AiUniversityResult> merged =
                                    mergeAiResults(guaranteedResults, aiResults);
                            showResults(
                                    merged,
                                    buildInsightSummary(merged, "AI-enhanced analysis")
                            );
                            Log.d(TAG, "AI-enhanced results: " + merged.size());
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            if (requestId != analysisGeneration || binding == null) return;
                            binding.tvAiInsight.setText(
                                    buildInsightSummary(
                                            guaranteedResults,
                                            "Smart weighted analysis"
                                    )
                            );
                            Log.w(TAG, "Remote AI unavailable; guaranteed results retained: "
                                    + errorMessage);
                        });
                    }
                }
        );
    }

    private void showResults(List<AiUniversityResult> results, String insight) {
        stopMsgAnimation();
        allResults = new ArrayList<>(results);
        binding.tvAiInsight.setText(insight);
        binding.aiInsightBox.setVisibility(View.VISIBLE);
        applyFilterAndSort();
        showAnalyzing(false);
    }

    private List<AiUniversityResult> mergeAiResults(
            List<AiUniversityResult> guaranteed,
            List<AiUniversityResult> aiResults
    ) {
        List<AiUniversityResult> merged = new ArrayList<>();
        boolean[] used = new boolean[aiResults.size()];

        for (AiUniversityResult local : guaranteed) {
            AiUniversityResult ai = null;
            int matchIndex = -1;
            String localName = normalizeName(local.getUniversityName());

            for (int index = 0; index < aiResults.size(); index++) {
                if (used[index]) continue;
                AiUniversityResult candidate = aiResults.get(index);
                String aiName = normalizeName(candidate.getUniversityName());
                if (localName.equals(aiName)
                        || (!localName.isEmpty() && aiName.contains(localName))
                        || (!aiName.isEmpty() && localName.contains(aiName))) {
                    ai = candidate;
                    matchIndex = index;
                    break;
                }
            }

            if (ai == null) {
                merged.add(local);
                continue;
            }

            used[matchIndex] = true;
            int score = ai.getEligibilityPercentage() > 0
                    ? Math.max(1, Math.min(100, ai.getEligibilityPercentage()))
                    : local.getEligibilityPercentage();
            ai.setEligibilityPercentage(score);
            if (!isValidCategory(ai.getCategory())) {
                ai.setCategory(categoryFor(score));
            }
            if (ai.getStrengths() == null || ai.getStrengths().isEmpty()) {
                ai.setStrengths(local.getStrengths());
            }
            if (ai.getWeakAreas() == null || ai.getWeakAreas().isEmpty()) {
                ai.setWeakAreas(local.getWeakAreas());
            }
            if (isBlank(ai.getScholarshipChance())) {
                ai.setScholarshipChance(local.getScholarshipChance());
            }
            if (isBlank(ai.getVisaInsight())) {
                ai.setVisaInsight(local.getVisaInsight());
            }
            if (isBlank(ai.getConsultantRecommendation())) {
                ai.setConsultantRecommendation(local.getConsultantRecommendation());
            }

            ai.setCourseName(local.getCourseName());
            ai.setCountry(local.getCountry());
            ai.setVisaRate(local.getVisaRate());
            ai.setAcceptanceRate(local.getAcceptanceRate());
            ai.setGpaRequirement(local.getGpaRequirement());
            merged.add(ai);
        }
        return merged;
    }

    private String normalizeName(String value) {
        if (value == null) return "";
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    private boolean isValidCategory(String category) {
        return "Safe".equals(category)
                || "Target".equals(category)
                || "Ambitious".equals(category)
                || "Low Chance".equals(category);
    }

    private String categoryFor(int score) {
        if (score >= 90) return "Safe";
        if (score >= 70) return "Target";
        if (score >= 50) return "Ambitious";
        return "Low Chance";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ─────────────────────────────────────────────
    // FILTER + SORT
    // ─────────────────────────────────────────────

    private void applyFilterAndSort() {
        List<AiUniversityResult> data = new ArrayList<>(allResults);
        String query = binding.etSearch.getText()
                .toString().trim().toLowerCase();

        switch (activeFilter) {
            case "safe": data = filterByCategory(data, "Safe"); break;
            case "target": data = filterByCategory(data, "Target"); break;
            case "ambitious": data = filterByCategory(data, "Ambitious"); break;
            case "high_schol": data = filterByScholarship(data); break;
            case "high_visa": data = filterByVisaInsight(data); break;
        }

        if (!query.isEmpty()) {
            List<AiUniversityResult> searched = new ArrayList<>();
            for (AiUniversityResult r : data) {
                if (r.getUniversityName().toLowerCase().contains(query)
                        || r.getCategory().toLowerCase().contains(query)) {
                    searched.add(r);
                }
            }
            data = searched;
        }

        switch (sortMode) {
            case "priority":
                Collections.sort(data, (a, b) -> a.getApplyPriority() - b.getApplyPriority());
                break;
            case "name":
                Collections.sort(data, (a, b) -> a.getUniversityName().compareTo(b.getUniversityName()));
                break;
            default:
                Collections.sort(data, (a, b) -> b.getEligibilityPercentage() - a.getEligibilityPercentage());
                break;
        }

        binding.tvMatchCount.setText("Showing " + data.size() + " results");
        binding.layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvMatches.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);

        adapter.submitList(new ArrayList<>(data));
    }

    private List<AiUniversityResult> filterByCategory(List<AiUniversityResult> list, String cat) {
        List<AiUniversityResult> r = new ArrayList<>();
        for (AiUniversityResult x : list) {
            if (x.getCategory().equals(cat)) r.add(x);
        }
        return r;
    }

    private List<AiUniversityResult> filterByScholarship(List<AiUniversityResult> list) {
        List<AiUniversityResult> r = new ArrayList<>();
        for (AiUniversityResult x : list) {
            if (x.getScholarshipChance().equals("High") || x.getScholarshipChance().equals("Medium")) r.add(x);
        }
        return r;
    }

    private List<AiUniversityResult> filterByVisaInsight(List<AiUniversityResult> list) {
        List<AiUniversityResult> r = new ArrayList<>();
        for (AiUniversityResult x : list) {
            String vi = x.getVisaInsight().toLowerCase();
            if (vi.contains("stable") || vi.contains("good") || vi.contains("high") || vi.contains("strong")) r.add(x);
        }
        return r;
    }

    // ─────────────────────────────────────────────
    // BUILD PROFILE FROM FIRESTORE
    // ─────────────────────────────────────────────

    private UserProfile buildProfile(DocumentSnapshot doc) {
        UserProfile p = new UserProfile();
        // 🔥 FIXED: Exact wahi keys daali hain jo aapki ProfileActivity use kar rahi hai
        p.setName(safeStr(doc, Constants.KEY_FULL_NAME));
        p.setGpa(safeDouble(doc, "cgpa"));
        p.setIelts(safeDouble(doc, "bands"));
        p.setDegreeLevel(safeStr(doc, Constants.KEY_QUALIFICATION));
        p.setPreferredCountry(safeStr(doc, Constants.KEY_TARGET_COUNTRIES));

        // Baqi cheezein waise hi hain (Agar database mein nahi bhi hongi toh safe getters inko sambhal lenge)
        p.setExperienceYears(safeInt(doc, "experience_years"));
        p.setField(safeStr(doc, Constants.KEY_INTERESTED_FIELDS));
        p.setBudget(safeStr(doc, "budget"));
        p.setStudyGap(safeStr(doc, "study_gap"));
        p.setPreferredIntake(safeStr(doc, "preferred_intake"));
        return p;
    }

    private List<UniversityMatch> parseUniversities(QuerySnapshot snap) {
        List<UniversityMatch> list = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            if (list.size() >= 8) break;
            try {
                Object programsValue = doc.get("programs");
                List<?> programs = programsValue instanceof List ? (List<?>) programsValue : new ArrayList<>();
                if (programs.isEmpty()) list.add(mapUniversity(doc, null));
                else {
                    // One representative program per university prevents duplicate cards and
                    // keeps the AI request within the provider's token/credit limits.
                    for (Object value : programs) {
                        if (value instanceof java.util.Map) {
                            list.add(mapUniversity(doc, (java.util.Map<?, ?>) value));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Skip malformed uni: " + e.getMessage());
            }
        }
        return list;
    }

    private UniversityMatch mapUniversity(DocumentSnapshot doc, java.util.Map<?, ?> program) {
        UniversityMatch u = new UniversityMatch();
        u.setId(doc.getId());
        u.setName(safeStr(doc, "name"));
        u.setCountry(safeStr(doc, "location"));
        u.setCountryName(safeStr(doc, "location"));
        u.setProgram(mapString(program, "course_name"));
        u.setLevel(mapString(program, "degree_level"));
        String programFees = mapString(program, "yearly_fees");
        u.setFees(programFees.isEmpty() ? safeStr(doc, "fees") : programFees);
        u.setDuration(mapString(program, "duration"));
        u.setVisaRatePakistan(percent(safeStr(doc, "visaRatio")));
        u.setAcceptanceRate(percent(safeStr(doc, "acceptanceRate")));
        u.setScholarshipAvailable(safeInt(doc, "scholarshipCount") > 0 ||
                (doc.get("scholarships") instanceof List && !((List<?>) doc.get("scholarships")).isEmpty()));
        u.setGpaRequired(firstPositive(
                mapDouble(program, "gpa_required", "gpaRequired", "min_gpa", "minimum_gpa"),
                safeDouble(doc, "gpaRequired"),
                safeDouble(doc, "gpa_required"),
                safeDouble(doc, "minGpa")
        ));
        u.setIeltsRequired(firstPositive(
                mapDouble(program, "ielts_required", "ieltsRequired", "min_ielts", "minimum_ielts"),
                safeDouble(doc, "ieltsRequired"),
                safeDouble(doc, "ielts_required"),
                safeDouble(doc, "minIelts")
        ));
        return u;
    }

    private String mapString(java.util.Map<?, ?> map, String key) {
        if (map == null || map.get(key) == null) return "";
        return String.valueOf(map.get(key)).trim();
    }

    private double mapDouble(java.util.Map<?, ?> map, String... keys) {
        if (map == null) return 0;
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) continue;
            if (value instanceof Number) return ((Number) value).doubleValue();
            try {
                String clean = String.valueOf(value).replaceAll("[^0-9.]", "");
                if (!clean.isEmpty()) return Double.parseDouble(clean);
            } catch (Exception ignored) {
                // Try the next supported key.
            }
        }
        return 0;
    }

    private double firstPositive(double... values) {
        for (double value : values) {
            if (value > 0) return value;
        }
        return 0;
    }

    private int percent(String value) {
        if (value == null) return 0;
        String clean = value.replaceAll("[^0-9.]", "");
        try { return clean.isEmpty() ? 0 : Math.round(Float.parseFloat(clean)); }
        catch (Exception ignored) { return 0; }
    }

    // ─────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────

    private void updateProfileUI(UserProfile p) {
        binding.tvUserName.setText(p.getName());
        binding.tvUserLevel.setText(
                p.getDegreeLevel().toUpperCase() + " " + formatField(p.getField())
        );
        binding.tvGpa.setText(String.valueOf(p.getGpa()));
        binding.tvIelts.setText(String.valueOf(p.getIelts()));
        binding.tvExperience.setText(p.getExperienceYears() + "yr");
    }

    private String buildInsightSummary(
            List<AiUniversityResult> results,
            String analysisLabel
    ) {
        int safe = 0, target = 0, ambitious = 0, highSchol = 0;
        for (AiUniversityResult r : results) {
            if (r.getCategory().equals("Safe")) safe++;
            if (r.getCategory().equals("Target")) target++;
            if (r.getCategory().equals("Ambitious")) ambitious++;
            if (r.getScholarshipChance().equals("High")) highSchol++;
        }
        return analysisLabel + " reviewed " + results.size() + " universities: "
                + safe + " Safe, " + target + " Target, " + ambitious + " Ambitious. "
                + highSchol + " have a high scholarship chance.";
    }

    private void showDetailSheet(AiUniversityResult result) {
        AiResultDetailSheet sheet = AiResultDetailSheet.newInstance(result);
        sheet.show(getSupportFragmentManager(), "ai_detail");
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private String formatField(String f) {
        if (f == null || f.isEmpty()) return "";
        String r = f.replace("_", " ");
        return r.substring(0, 1).toUpperCase() + r.substring(1);
    }

    private void showAnalyzing(boolean show) {
        binding.layoutAnalyzing.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(show  ? View.GONE    : View.VISIBLE);
    }

    // 🔥 FIXED: Bulletproof Firestore Getters (String aur Number dono handle karega)
    private String safeStr(DocumentSnapshot d, String k) {
        Object val = d.get(k);
        return val != null ? String.valueOf(val).trim() : "";
    }

    private double safeDouble(DocumentSnapshot d, String k) {
        Object val = d.get(k);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); }
            catch (Exception e) { return 0.0; }
        }
        return 0.0;
    }

    private int safeInt(DocumentSnapshot d, String k) {
        Object val = d.get(k);
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); }
            catch (Exception e) { return 0; }
        }
        return 0;
    }

    // ── Msg animation ──
    private boolean isAnimating() {
        return handler != null && msgRunnable != null;
    }

    private void startMsgAnimation() {
        msgIdx  = 0;
        handler = new Handler(Looper.getMainLooper());
        msgRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && msgIdx < analyzeMessages.length) {
                    binding.tvAnalyzeStatus.setText(analyzeMessages[msgIdx++]);
                    handler.postDelayed(this, 700);
                }
            }
        };
        handler.post(msgRunnable);
    }

    private void stopMsgAnimation() {
        if (handler != null && msgRunnable != null) {
            handler.removeCallbacks(msgRunnable);
            handler   = null;
            msgRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        analysisGeneration++;
        stopMsgAnimation();
        binding = null;
    }
}
