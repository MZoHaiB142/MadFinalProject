package com.example.madfinalproject.engine;

import android.util.Log;

import com.example.madfinalproject.models.AiUniversityResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AiResultParser {

    private static final String TAG = "AiResultParser";

    // ── Parse Claude's JSON response → List of results ──
    public static List<AiUniversityResult> parse(String aiResponse) {
        List<AiUniversityResult> results = new ArrayList<>();

        try {
            // Claude sometimes wraps in markdown — clean it
            String cleaned = cleanJson(aiResponse);

            JSONArray arr;
            if (cleaned.startsWith("{")) {
                JSONObject root = new JSONObject(cleaned);
                arr = root.optJSONArray("results");
                if (arr == null) {
                    JSONObject single = root.optJSONObject("result");
                    if (single == null && root.has("university_name")) single = root;
                    if (single == null) {
                        Log.e(TAG, "AI response does not contain a results array");
                        return results;
                    }
                    arr = new JSONArray().put(single);
                }
            } else {
                arr = new JSONArray(cleaned);
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                AiUniversityResult result = new AiUniversityResult();

                result.setUniversityName(
                        obj.optString("university_name", "Unknown"));
                result.setEligibilityPercentage(
                        clamp(readInt(obj, "eligibility_percentage"), 0, 100));
                result.setCategory(
                        obj.optString("category", "Low Chance"));
                result.setScholarshipChance(
                        obj.optString("scholarship_chance", "Low"));
                result.setVisaInsight(
                        obj.optString("visa_insight", ""));
                result.setConsultantRecommendation(
                        obj.optString("consultant_recommendation", ""));
                result.setApplyPriority(
                        obj.optInt("apply_priority", i + 1));

                result.setStrengths(readStringList(obj, "strengths"));
                result.setWeakAreas(readStringList(obj, "weak_areas"));

                if (!"Unknown".equalsIgnoreCase(result.getUniversityName().trim())) {
                    results.add(result);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Parse failed: " + e.getMessage()
                    + "\nRaw: " + aiResponse);
        }

        return results;
    }

    // Clean markdown code blocks if present
    private static String cleanJson(String raw) {
        if (raw == null) return "";
        String cleaned = raw.trim();

        // Remove ```json ... ``` wrappers
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        cleaned = cleaned.trim();
        int objectStart = cleaned.indexOf('{');
        int arrayStart = cleaned.indexOf('[');
        int start;
        if (objectStart < 0) start = arrayStart;
        else if (arrayStart < 0) start = objectStart;
        else start = Math.min(objectStart, arrayStart);

        int objectEnd = cleaned.lastIndexOf('}');
        int arrayEnd = cleaned.lastIndexOf(']');
        int end = Math.max(objectEnd, arrayEnd);
        if (start >= 0 && end >= start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned.trim();
    }

    private static int readInt(JSONObject object, String key) {
        Object value = object.opt(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value == null) return 0;
        try {
            String clean = String.valueOf(value).replaceAll("[^0-9-]", "");
            return clean.isEmpty() ? 0 : Integer.parseInt(clean);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static List<String> readStringList(JSONObject object, String key) {
        List<String> values = new ArrayList<>();
        JSONArray array = object.optJSONArray(key);
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                String value = array.optString(index, "").trim();
                if (!value.isEmpty()) values.add(value);
            }
            return values;
        }

        String single = object.optString(key, "").trim();
        if (!single.isEmpty()) values.add(single);
        return values;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
