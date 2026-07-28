package com.example.madfinalproject.engine;

import android.util.Log;

import com.example.madfinalproject.BuildConfig;
import com.example.madfinalproject.models.UniversityMatch;
import com.example.madfinalproject.models.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiEligibilityEngine {

    private static final String TAG = "AiEligibilityEngine";

    // ✅ Google Gemini API config (Aapki working ChatActivity wali key aur URL)
    private static final String API_URL =
            (BuildConfig.AI_BASE_URL.endsWith("/")
                    ? BuildConfig.AI_BASE_URL
                    : BuildConfig.AI_BASE_URL + "/")
                    + "chat/completions";

    // OkHttp client with timeout
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .callTimeout(22, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    // ── Callback Interface ──
    public interface AiResponseCallback {
        void onSuccess(String aiResponse);
        void onError(String errorMessage);
    }

    // ─────────────────────────────────────────────────────────
    // MAIN METHOD
    // ─────────────────────────────────────────────────────────
    public static void analyzeEligibility(
            UserProfile          profile,
            List<UniversityMatch> universities,
            AiResponseCallback    callback
    ) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userMessage  = buildUserMessage(profile, universities);

            // Gemini ko call jayegi
            callAiApi(systemPrompt, userMessage, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error building request: " + e.getMessage());
            callback.onError("Request build error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // SYSTEM PROMPT
    // ─────────────────────────────────────────────────────────
    private static String buildSystemPrompt() {
        return "You are AbroadIQ's university eligibility consultant. "
                + "Evaluate only the supplied universities; never invent data. "
                + "Weights: English 30, GPA 30, budget 15, visa ratio 10, "
                + "acceptance rate 10, scholarship 5. "
                + "Categories: Safe 90-100, Target 70-89, Ambitious 50-69, "
                + "Low Chance below 50. Return only compact valid JSON in this schema: "
                + "{\"results\":[{\"university_name\":\"\","
                + "\"eligibility_percentage\":0,\"category\":\"Target\","
                + "\"strengths\":[\"\",\"\"],\"weak_areas\":[\"\",\"\"],"
                + "\"scholarship_chance\":\"Medium\",\"visa_insight\":\"\","
                + "\"consultant_recommendation\":\"\",\"apply_priority\":1}]}. "
                + "Keep insight and recommendation under 18 words each.";
    }

    // ─────────────────────────────────────────────────────────
    // USER MESSAGE
    // ─────────────────────────────────────────────────────────
    private static String buildUserMessage(
            UserProfile profile,
            List<UniversityMatch> universities
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: GPA=").append(profile.getGpa())
                .append(", IELTS/PTE=").append(profile.getIelts())
                .append(", qualification=").append(profile.getDegreeLevel())
                .append(", field=").append(profile.getField())
                .append(", experienceYears=").append(profile.getExperienceYears())
                .append(", budget=").append(profile.getBudget())
                .append(", preferredCountry=").append(profile.getPreferredCountry())
                .append(". Universities:\n");

        for (int i = 0; i < universities.size(); i++) {
            UniversityMatch u = universities.get(i);
            sb.append(i + 1).append("|").append(u.getName())
                    .append("|country=").append(u.getCountryName())
                    .append("|program=").append(u.getProgram())
                    .append("|ielts=").append(u.getIeltsRequired())
                    .append("|gpa=").append(u.getGpaRequired())
                    .append("|fee=").append(u.getFees())
                    .append("|acceptance=").append(u.getAcceptanceRate())
                    .append("|scholarship=").append(u.isScholarshipAvailable())
                    .append("|visa=").append(u.getVisaRatePakistan())
                    .append("\n");
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────
    // API CALL — Google Gemini Free API (Fixed to match ChatActivity logic)
    // ─────────────────────────────────────────────────────────
    private static void callAiApi(
            String systemPrompt,
            String userMessage,
            AiResponseCallback callback
    ) {
        try {
            // 🔥 Yahan JSON bilkul waise hi banaya hai jaise aapki ChatActivity mein tha
            if (BuildConfig.OPENAI_API_KEY == null || BuildConfig.OPENAI_API_KEY.trim().isEmpty()) {
                callback.onError("AI API key is not configured.");
                return;
            }

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", BuildConfig.OPENAI_MODEL);
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userMessage));
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.2);
            // Compact prompts and output keep latency and provider cost low.
            jsonBody.put("max_tokens", 1100);
            jsonBody.put("response_format", new JSONObject().put("type", "json_object"));

            RequestBody requestBody = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Title", "AbroadIQ")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API failed: " + e.getMessage());
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    // 🔥 NAYA ERROR CHECKING: Agar error aaye toh exact wajah JSON se nikal kar screen par dikhaye
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API error raw: " + responseBody);
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String exactError = errorJson.getJSONObject("error").getString("message");
                            callback.onError(exactError); // Yeh screen par aayega
                        } catch (Exception ex) {
                            callback.onError("AI service error: " + response.code());
                        }
                        return;
                    }

                    try {
                        // Gemini ki response read karna
                        JSONObject json = new JSONObject(responseBody);
                        String text = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        Log.d(TAG, "AI response received, length=" + text.length());
                        callback.onSuccess(text.trim());

                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage() + "\nRaw: " + responseBody);
                        callback.onError("Response parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Request build error: " + e.getMessage());
            callback.onError("Error: " + e.getMessage());
        }
    }
}
