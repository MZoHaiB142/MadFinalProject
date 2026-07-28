package com.example.madfinalproject.models;

import com.google.firebase.Timestamp;

public class SopRequest {

    private String    userId;
    private String    country;
    private String    university;
    private String    course;

    // Naye Academic Fields
    private String    education;
    private String    cgpa;
    private String    finalYearProject;

    private String    experience;

    // Naye Career Goals Fields
    private String    future_goals;
    private String    targetIndustry;

    // Naye Financial Fields
    private String    financial_support;
    private String    sponsorRelationship;
    private String    sponsorProfession;

    private String    study_gap;
    private Timestamp createdAt;
    private String keySubjects;
    private String    status; // pending | processing | completed | failed

    public SopRequest() {}

    // Full constructor (Updated)
    public SopRequest(
            String userId, String country, String university, String course, String keySubjects,
            String education, String cgpa, String finalYearProject,
            String experience, String futureGoals, String targetIndustry,
            String financialSupport, String sponsorRelationship, String sponsorProfession,
            String studyGap
    ) {
        this.userId              = userId;
        this.country             = country;
        this.university          = university;
        this.course              = course;
        this.keySubjects         = keySubjects; // 🔥 Naya
        this.education           = education;
        this.cgpa                = cgpa;
        this.finalYearProject    = finalYearProject;
        this.experience          = experience;
        this.future_goals        = futureGoals;
        this.targetIndustry      = targetIndustry;
        this.financial_support   = financialSupport;
        this.sponsorRelationship = sponsorRelationship;
        this.sponsorProfession   = sponsorProfession;
        this.study_gap           = studyGap;
        this.createdAt           = Timestamp.now();
        this.status              = "pending";
    }
    // ────────────────────────────
    // Getters
    // ────────────────────────────
    public String    getUserId()              { return userId; }
    public String    getCountry()             { return country; }
    public String    getUniversity()          { return university; }
    public String    getCourse()              { return course; }
    public String    getEducation()           { return education; }
    public String    getCgpa()                { return cgpa; }
    public String    getFinalYearProject()    { return finalYearProject; }
    public String    getExperience()          { return experience; }
    public String    getFutureGoals()         { return future_goals; }
    public String    getTargetIndustry()      { return targetIndustry; }
    public String    getFinancialSupport()    { return financial_support; }
    public String    getSponsorRelationship() { return sponsorRelationship; }
    public String    getSponsorProfession()   { return sponsorProfession; }
    public String    getStudyGap()            { return study_gap; }
    public Timestamp getCreatedAt()           { return createdAt; }
    public String    getStatus()              { return status; }
    public String getKeySubjects() { return keySubjects; }

    // ────────────────────────────
    // Setters
    // ────────────────────────────
    public void setUserId(String v)              { userId = v; }
    public void setCountry(String v)             { country = v; }
    public void setUniversity(String v)          { university = v; }
    public void setCourse(String v)              { course = v; }
    public void setEducation(String v)           { education = v; }
    public void setCgpa(String v)                { cgpa = v; }
    public void setFinalYearProject(String v)    { finalYearProject = v; }
    public void setExperience(String v)          { experience = v; }
    public void setFutureGoals(String v)         { future_goals = v; }
    public void setTargetIndustry(String v)      { targetIndustry = v; }
    public void setFinancialSupport(String v)    { financial_support = v; }
    public void setSponsorRelationship(String v) { sponsorRelationship = v; }
    public void setSponsorProfession(String v)   { sponsorProfession = v; }
    public void setStudyGap(String v)            { study_gap = v; }
    public void setCreatedAt(Timestamp v)        { createdAt = v; }
    public void setStatus(String v)              { status = v; }
    public void setKeySubjects(String v) { keySubjects = v; }
}