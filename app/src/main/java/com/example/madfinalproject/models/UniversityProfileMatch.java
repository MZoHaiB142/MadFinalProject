package com.example.madfinalproject.models;

import java.util.ArrayList;
import java.util.List;

public class UniversityProfileMatch {

    private int score;
    private String category = "Low Chance";
    private String suitabilityLabel = "Weak Match";
    private String summary = "";
    private String consultantAdvice = "";
    private String evaluatedSignals = "";
    private boolean profileComplete;
    private boolean aiEnhanced;
    private List<String> strengths = new ArrayList<>();
    private List<String> weakPoints = new ArrayList<>();

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = safe(category);
    }

    public String getSuitabilityLabel() {
        return suitabilityLabel;
    }

    public void setSuitabilityLabel(String suitabilityLabel) {
        this.suitabilityLabel = safe(suitabilityLabel);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = safe(summary);
    }

    public String getConsultantAdvice() {
        return consultantAdvice;
    }

    public void setConsultantAdvice(String consultantAdvice) {
        this.consultantAdvice = safe(consultantAdvice);
    }

    public String getEvaluatedSignals() {
        return evaluatedSignals;
    }

    public void setEvaluatedSignals(String evaluatedSignals) {
        this.evaluatedSignals = safe(evaluatedSignals);
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public boolean isAiEnhanced() {
        return aiEnhanced;
    }

    public void setAiEnhanced(boolean aiEnhanced) {
        this.aiEnhanced = aiEnhanced;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths == null ? new ArrayList<>() : new ArrayList<>(strengths);
    }

    public List<String> getWeakPoints() {
        return weakPoints;
    }

    public void setWeakPoints(List<String> weakPoints) {
        this.weakPoints = weakPoints == null ? new ArrayList<>() : new ArrayList<>(weakPoints);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
