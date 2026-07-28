package com.example.madfinalproject.models;

import java.util.Map;

public class SopRules {

    // Main field
    private String country;

    // 🔥 NEW JSON STRUCTURE MAPS 🔥
    private Map<String, Object> personalization_requirements;
    private Map<String, Object> personal_motivation_section;
    private Map<String, Object> anti_ai_patterns;
    private Map<String, Object> why_university_enhancement;
    private Map<String, Object> why_not_home_country;
    private Map<String, Object> return_intent_strengthening;
    private Map<String, Object> genuine_student_requirements;
    private Map<String, Object> quality_checks;
    private Map<String, Object> humanization_rules;
    private Map<String, Object> professional_experience_rules;
    private Map<String, Object> course_justification_rules;
    private Map<String, Object> career_goal_rules;
    private Map<String, Object> financial_viability_enhancement;
    private Map<String, Object> sop_final_scoring;

    private Map<String, Object> word_count_rules;

    // Empty constructor for Firestore
    public SopRules() {}

    // ────────────────────────────
    // Getters
    // ────────────────────────────
    public String getCountry() { return country; }
    public Map<String, Object> getPersonalizationRequirements() { return personalization_requirements; }
    public Map<String, Object> getPersonalMotivationSection() { return personal_motivation_section; }
    public Map<String, Object> getAntiAiPatterns() { return anti_ai_patterns; }
    public Map<String, Object> getWhyUniversityEnhancement() { return why_university_enhancement; }
    public Map<String, Object> getWhyNotHomeCountry() { return why_not_home_country; }
    public Map<String, Object> getReturnIntentStrengthening() { return return_intent_strengthening; }
    public Map<String, Object> getGenuineStudentRequirements() { return genuine_student_requirements; }
    public Map<String, Object> getQualityChecks() { return quality_checks; }
    public Map<String, Object> getHumanizationRules() { return humanization_rules; }
    public Map<String, Object> getProfessionalExperienceRules() { return professional_experience_rules; }
    public Map<String, Object> getCourseJustificationRules() { return course_justification_rules; }
    public Map<String, Object> getCareerGoalRules() { return career_goal_rules; }
    public Map<String, Object> getFinancialViabilityEnhancement() { return financial_viability_enhancement; }
    public Map<String, Object> getSopFinalScoring() { return sop_final_scoring; }
    public Map<String, Object> getWordCountRules() { return word_count_rules; }

    // ────────────────────────────
    // Setters
    // ────────────────────────────
    public void setCountry(String country) { this.country = country; }
    public void setPersonalizationRequirements(Map<String, Object> v) { this.personalization_requirements = v; }
    public void setPersonalMotivationSection(Map<String, Object> v) { this.personal_motivation_section = v; }
    public void setAntiAiPatterns(Map<String, Object> v) { this.anti_ai_patterns = v; }
    public void setWhyUniversityEnhancement(Map<String, Object> v) { this.why_university_enhancement = v; }
    public void setWhyNotHomeCountry(Map<String, Object> v) { this.why_not_home_country = v; }
    public void setReturnIntentStrengthening(Map<String, Object> v) { this.return_intent_strengthening = v; }
    public void setGenuineStudentRequirements(Map<String, Object> v) { this.genuine_student_requirements = v; }
    public void setQualityChecks(Map<String, Object> v) { this.quality_checks = v; }
    public void setHumanizationRules(Map<String, Object> v) { this.humanization_rules = v; }
    public void setProfessionalExperienceRules(Map<String, Object> v) { this.professional_experience_rules = v; }
    public void setCourseJustificationRules(Map<String, Object> v) { this.course_justification_rules = v; }
    public void setCareerGoalRules(Map<String, Object> v) { this.career_goal_rules = v; }
    public void setFinancialViabilityEnhancement(Map<String, Object> v) { this.financial_viability_enhancement = v; }
    public void setSopFinalScoring(Map<String, Object> v) { this.sop_final_scoring = v; }
    public void setWordCountRules(Map<String, Object> v) { this.word_count_rules = v; }
}