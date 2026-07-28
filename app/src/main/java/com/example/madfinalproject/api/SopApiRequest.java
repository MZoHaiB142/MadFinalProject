package com.example.madfinalproject.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class SopApiRequest {

    // ── SYSTEM INFO ──
    @SerializedName("user_id") private String userId;
    @SerializedName("request_id") private String requestId;

    // ── STUDY PLAN ──
    @SerializedName("country") private String country;
    @SerializedName("university") private String university;
    @SerializedName("course") private String course;
    @SerializedName("intake") private String intake;

    // ── ACADEMIC BACKGROUND ──
    @SerializedName("education") private String education;
    @SerializedName("current_qualification") private String currentQualification;
    @SerializedName("graduation_year") private Integer graduationYear;
    @SerializedName("cgpa") private String cgpa;
    @SerializedName("major_subjects") private List<String> majorSubjects;
    @SerializedName("final_year_project") private String finalYearProject;
    @SerializedName("academic_achievements") private List<String> academicAchievements;
    @SerializedName("study_gap") private String studyGap;

    // ── PROFESSIONAL EXPERIENCE ──
    @SerializedName("experience") private String experience;
    @SerializedName("internships") private List<String> internships;
    @SerializedName("technical_skills") private List<String> technicalSkills;
    @SerializedName("certifications") private List<String> certifications;

    // ── CAREER GOALS & RETURN INTENT ──
    @SerializedName("future_goals") private String futureGoals;
    @SerializedName("target_industry") private String targetIndustry;
    @SerializedName("target_job_role") private String targetJobRole;
    @SerializedName("family_ties") private String familyTies;
    @SerializedName("home_country_ties") private String homeCountryTies;

    // ── FINANCIAL INFORMATION ──
    @SerializedName("financial_support") private String financialSupport;
    @SerializedName("sponsor_name") private String sponsorName;
    @SerializedName("sponsor_relationship") private String sponsorRelationship;
    @SerializedName("sponsor_profession") private String sponsorProfession;
    @SerializedName("annual_income") private String annualIncome;
    @SerializedName("available_funds") private String availableFunds;

    // ── SOP GENERATION RULES (Naye Maps) ──
    @SerializedName("word_count_rules") private Map<String, Object> wordCountRules;
    @SerializedName("personalization_requirements") private Map<String, Object> personalizationRequirements;
    @SerializedName("personal_motivation_section") private Map<String, Object> personalMotivationSection;
    @SerializedName("anti_ai_patterns") private Map<String, Object> antiAiPatterns;
    @SerializedName("why_university_enhancement") private Map<String, Object> whyUniversityEnhancement;
    @SerializedName("why_not_home_country") private Map<String, Object> whyNotHomeCountry;
    @SerializedName("return_intent_strengthening") private Map<String, Object> returnIntentStrengthening;
    @SerializedName("genuine_student_requirements") private Map<String, Object> genuineStudentRequirements;
    @SerializedName("quality_checks") private Map<String, Object> qualityChecks;
    @SerializedName("humanization_rules") private Map<String, Object> humanizationRules;
    @SerializedName("professional_experience_rules") private Map<String, Object> professionalExperienceRules;
    @SerializedName("course_justification_rules") private Map<String, Object> courseJustificationRules;
    @SerializedName("career_goal_rules") private Map<String, Object> careerGoalRules;
    @SerializedName("financial_viability_enhancement") private Map<String, Object> financialViabilityEnhancement;
    @SerializedName("sop_final_scoring") private Map<String, Object> sopFinalScoring;
    @SerializedName("university_curriculum")
    private List<String> universityCurriculum;

    public SopApiRequest() {}

    // ── Getters & Setters ──
    // Note: Yahan aapne sab ke standard Getters/Setters banaye hue hain.
    // Android Studio mein right-click kar ke Generate > Getter and Setter kar lein sab fields ke liye.

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }
    public String getFinalYearProject() { return finalYearProject; }
    public void setFinalYearProject(String finalYearProject) { this.finalYearProject = finalYearProject; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public List<String> getInternships() { return internships; }
    public void setInternships(List<String> internships) { this.internships = internships; }
    public String getFutureGoals() { return futureGoals; }
    public void setFutureGoals(String futureGoals) { this.futureGoals = futureGoals; }
    public String getTargetIndustry() { return targetIndustry; }
    public void setTargetIndustry(String targetIndustry) { this.targetIndustry = targetIndustry; }
    public String getFinancialSupport() { return financialSupport; }
    public void setFinancialSupport(String financialSupport) { this.financialSupport = financialSupport; }
    public String getSponsorRelationship() { return sponsorRelationship; }
    public void setSponsorRelationship(String sponsorRelationship) { this.sponsorRelationship = sponsorRelationship; }
    public String getSponsorProfession() { return sponsorProfession; }
    public void setSponsorProfession(String sponsorProfession) { this.sponsorProfession = sponsorProfession; }
    public String getStudyGap() { return studyGap; }
    public void setStudyGap(String studyGap) { this.studyGap = studyGap; }
    public List<String> getUniversityCurriculum() { return universityCurriculum; }

    // Rules Setters
    public void setWordCountRules(Map<String, Object> v) { this.wordCountRules = v; }
    public void setPersonalizationRequirements(Map<String, Object> v) { this.personalizationRequirements = v; }
    public void setPersonalMotivationSection(Map<String, Object> v) { this.personalMotivationSection = v; }
    public void setAntiAiPatterns(Map<String, Object> v) { this.antiAiPatterns = v; }
    public void setWhyUniversityEnhancement(Map<String, Object> v) { this.whyUniversityEnhancement = v; }
    public void setWhyNotHomeCountry(Map<String, Object> v) { this.whyNotHomeCountry = v; }
    public void setReturnIntentStrengthening(Map<String, Object> v) { this.returnIntentStrengthening = v; }
    public void setGenuineStudentRequirements(Map<String, Object> v) { this.genuineStudentRequirements = v; }
    public void setQualityChecks(Map<String, Object> v) { this.qualityChecks = v; }
    public void setHumanizationRules(Map<String, Object> v) { this.humanizationRules = v; }
    public void setProfessionalExperienceRules(Map<String, Object> v) { this.professionalExperienceRules = v; }
    public void setCourseJustificationRules(Map<String, Object> v) { this.courseJustificationRules = v; }
    public void setCareerGoalRules(Map<String, Object> v) { this.careerGoalRules = v; }
    public void setFinancialViabilityEnhancement(Map<String, Object> v) { this.financialViabilityEnhancement = v; }
    public void setSopFinalScoring(Map<String, Object> v) { this.sopFinalScoring = v; }
    public void setUniversityCurriculum(List<String> universityCurriculum) { this.universityCurriculum = universityCurriculum; }
}