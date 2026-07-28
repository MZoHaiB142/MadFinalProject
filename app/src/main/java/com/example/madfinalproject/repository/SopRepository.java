package com.example.madfinalproject.repository;

import android.util.Log;

import com.example.madfinalproject.api.ApiClient;
import com.example.madfinalproject.api.SopApiRequest;
import com.example.madfinalproject.api.SopApiResponse;
import com.example.madfinalproject.models.GeneratedSop;
import com.example.madfinalproject.models.SopRequest;
import com.example.madfinalproject.models.SopRules;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SopRepository {

    private static final String TAG = "SopRepository";

    // Firestore collection names
    private static final String COL_SOP_RULES    = "sop_rules";
    private static final String COL_SOP_REQUESTS = "sop_requests";
    private static final String COL_GENERATED    = "generated_sops";

    private final FirebaseFirestore db;

    // ── Callback Interfaces ──
    public interface RulesCallback {
        void onSuccess(SopRules rules);
        void onError(String error);
    }

    public interface SopGeneratedCallback {
        void onSuccess(GeneratedSop sop);
        void onError(String error);
    }

    public interface SopSavedCallback {
        void onSuccess(String requestId);
        void onError(String error);
    }

    public interface SopListenerCallback {
        void onUpdate(GeneratedSop sop);
        void onError(String error);
    }

    public SopRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ─────────────────────────────────────────────
    // STEP 1: Firestore se SOP Rules fetch karo
    // ─────────────────────────────────────────────
    public void getSopRules(String country, RulesCallback callback) {

        // Country name lowercase karke document ID banao
        String docId = country.toLowerCase().trim().replace(" ", "_");

        db.collection(COL_SOP_RULES)
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SopRules rules = doc.toObject(SopRules.class);
                        if (rules != null) {
                            callback.onSuccess(rules);
                        } else {
                            callback.onError("Unable to process the SOP rules");
                        }
                    } else {
                        // Agar country ka doc na ho toh default rules use karo
                        callback.onSuccess(getDefaultRules(country));
                        Log.w(TAG, "No rules found for: " + country + " — using defaults");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Rules fetch failed: " + e.getMessage());
                    // Network error par bhi default rules de do
                    callback.onSuccess(getDefaultRules(country));
                });
    }

    // ─────────────────────────────────────────────
    // STEP 2: SopRequest Firestore mein save karo
    // ─────────────────────────────────────────────
    public void saveSopRequest(SopRequest request, SopSavedCallback callback) {
        db.collection(COL_SOP_REQUESTS)
                .add(request)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "SopRequest saved: " + docRef.getId());
                    callback.onSuccess(docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "SopRequest save failed: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ─────────────────────────────────────────────
    // STEP 3: FastAPI call karo SOP generate karne ke liye
    // ─────────────────────────────────────────────
    public void generateSopFromApi(
            SopApiRequest apiRequest,
            String        requestId,
            String        userId,
            SopGeneratedCallback callback
    ) {
        // Status update: processing
        updateRequestStatus(requestId, "processing");

        ApiClient.getSopService()
                .generateSop(apiRequest)
                .enqueue(new Callback<SopApiResponse>() {

                    @Override
                    public void onResponse(
                            Call<SopApiResponse> call,
                            Response<SopApiResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            SopApiResponse apiResp = response.body();

                            if (apiResp.isSuccess()) {
                                // GeneratedSop object banao
                                GeneratedSop sop = new GeneratedSop();
                                sop.setUserId(userId);
                                sop.setRequestId(requestId);
                                sop.setCountry(apiRequest.getCountry());
                                sop.setSopText(apiResp.getSopText());
                                sop.setScore(apiResp.getScore());
                                sop.setMotivationScore(apiResp.getMotivationScore());
                                sop.setVisaStrengthScore(apiResp.getVisaStrengthScore());
                                sop.setClarityScore(apiResp.getClarityScore());
                                sop.setCareerAlignmentScore(apiResp.getCareerAlignmentScore());
                                sop.setWordCount(apiResp.getWordCount());
                                sop.setStatus("completed");
                                sop.setCreatedAt(Timestamp.now());

                                // Firestore mein save karo
                                saveSopToFirestore(sop, requestId, callback);
                            } else {
                                updateRequestStatus(requestId, "failed");
                                callback.onError(apiResp.getError());
                            }

                        } else {
                            updateRequestStatus(requestId, "failed");
                            callback.onError("Server error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SopApiResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        updateRequestStatus(requestId, "failed");
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────
    // STEP 4: Generated SOP Firestore mein save karo
    // ─────────────────────────────────────────────
    private void saveSopToFirestore(
            GeneratedSop sop,
            String requestId,
            SopGeneratedCallback callback
    ) {
        db.collection(COL_GENERATED)
                .add(sop)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "SOP saved: " + docRef.getId());
                    // Request ka status bhi update karo
                    updateRequestStatus(requestId, "completed");
                    callback.onSuccess(sop);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "SOP save failed: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ─────────────────────────────────────────────
    // Realtime Listener — SOP result aane ka wait karo
    // ─────────────────────────────────────────────
    public ListenerRegistration listenForSop(
            String requestId,
            SopListenerCallback callback
    ) {
        return db.collection(COL_GENERATED)
                .whereEqualTo("requestId", requestId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        GeneratedSop sop = snapshots
                                .getDocuments()
                                .get(0)
                                .toObject(GeneratedSop.class);
                        if (sop != null) {
                            callback.onUpdate(sop);
                        }
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────
    private void updateRequestStatus(String requestId, String status) {
        if (requestId == null || requestId.isEmpty()) return;
        db.collection(COL_SOP_REQUESTS)
                .document(requestId)
                .update("status", status)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Status update failed: " + e.getMessage()));
    }

    // 🔥 UPDATED: Naye JSON Rules ke hisaab se Fallback Default Rules
    private SopRules getDefaultRules(String country) {
        SopRules rules = new SopRules();
        rules.setCountry(country);

        // Word Count Limits
        Map<String, Object> wordCount = new HashMap<>();
        wordCount.put("strict_limit", true);
        wordCount.put("minimum", 800);
        wordCount.put("maximum", 1000);
        wordCount.put("ideal", 900);
        rules.setWordCountRules(wordCount);

        // Personalization
        Map<String, Object> personalization = new HashMap<>();
        personalization.put("required", true);
        personalization.put("minimum_score", 8);
        rules.setPersonalizationRequirements(personalization);

        // Anti-AI
        Map<String, Object> antiAi = new HashMap<>();
        antiAi.put("required", true);
        rules.setAntiAiPatterns(antiAi);

        // Genuine Student
        Map<String, Object> genuineStudent = new HashMap<>();
        genuineStudent.put("required", true);
        rules.setGenuineStudentRequirements(genuineStudent);

        // Scoring
        Map<String, Object> scoring = new HashMap<>();
        scoring.put("personalization", 15);
        scoring.put("motivation_strength", 15);
        scoring.put("return_intent", 10);
        rules.setSopFinalScoring(scoring);

        return rules;
    }
}
