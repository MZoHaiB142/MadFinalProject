package com.example.madfinalproject.models;

import java.util.ArrayList;
import java.util.List;
public class AiUniversityResult {

    // ── Purane Variables ──
    private String       universityName         = "";
    private int          eligibilityPercentage  = 0;
    private String       category               = "Low Chance";
    private List<String> strengths              = new ArrayList<>();
    private List<String> weakAreas              = new ArrayList<>();
    private String       scholarshipChance      = "Low";
    private String       visaInsight            = "";
    private String       consultantRecommendation = "";
    private int          applyPriority          = 1;

    // 🔥 NAYE VARIABLES (New UI ke liye) 🔥
    private String       courseName             = "";
    private String       country                = "";
    private double       visaRate               = 0.0;
    private double       acceptanceRate         = 0.0;
    private double       gpaRequirement         = 0.0;

    // ── Purane Getters ──
    public String       getUniversityName()          { return universityName; }
    public int          getEligibilityPercentage()   { return eligibilityPercentage; }
    public String       getCategory()                { return category; }
    public List<String> getStrengths()               { return strengths; }
    public List<String> getWeakAreas()               { return weakAreas; }
    public String       getScholarshipChance()       { return scholarshipChance; }
    public String       getVisaInsight()             { return visaInsight; }
    public String       getConsultantRecommendation(){ return consultantRecommendation; }
    public int          getApplyPriority()           { return applyPriority; }

    // 🔥 NAYE GETTERS 🔥
    public String       getCourseName()              { return courseName; }
    public String       getCountry()                 { return country; }
    public double       getVisaRate()                { return visaRate; }
    public double       getAcceptanceRate()          { return acceptanceRate; }
    public double       getGpaRequirement()          { return gpaRequirement; }

    // ── Purane Setters ──
    public void setUniversityName(String v)           { universityName = v; }
    public void setEligibilityPercentage(int v)       { eligibilityPercentage = v; }
    public void setCategory(String v)                 { category = v; }
    public void setStrengths(List<String> v)          { strengths = v; }
    public void setWeakAreas(List<String> v)          { weakAreas = v; }
    public void setScholarshipChance(String v)        { scholarshipChance = v; }
    public void setVisaInsight(String v)              { visaInsight = v; }
    public void setConsultantRecommendation(String v) { consultantRecommendation = v; }
    public void setApplyPriority(int v)               { applyPriority = v; }

    // 🔥 NAYE SETTERS 🔥
    public void setCourseName(String v)               { courseName = v; }
    public void setCountry(String v)                  { country = v; }
    public void setVisaRate(double v)                 { visaRate = v; }
    public void setAcceptanceRate(double v)           { acceptanceRate = v; }
    public void setGpaRequirement(double v)           { gpaRequirement = v; }

    // Helper: category color resource name
    public String getCategoryColorName() {
        switch (category) {
            case "Safe":       return "green";
            case "Target":     return "blue";
            case "Ambitious":  return "amber";
            default:           return "red";
        }
    }

    // Helper: scholarship color
    public String getScholarshipColorName() {
        switch (scholarshipChance) {
            case "High":     return "green";
            case "Medium":   return "amber";
            case "Low":      return "red";
            default:         return "text_secondary";
        }
    }
}