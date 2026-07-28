package com.example.madfinalproject.models;

public class UserProfile {

    private String name             = "";
    private double gpa              = 0.0;
    private double ielts            = 0.0;
    private int    experienceYears  = 0;
    private String degreeLevel      = "ms";
    private String field            = "";

    // New fields for AI engine
    private String budget           = "";
    private String preferredCountry = "";
    private String studyGap         = "None";
    private String preferredIntake  = "September";

    public UserProfile() {}

    public UserProfile(
            String name, double gpa, double ielts,
            int exp, String level, String field
    ) {
        this.name           = name;
        this.gpa            = gpa;
        this.ielts          = ielts;
        this.experienceYears = exp;
        this.degreeLevel    = level;
        this.field          = field;
    }

    // Getters
    public String getName()             { return name; }
    public double getGpa()              { return gpa; }
    public double getIelts()            { return ielts; }
    public int    getExperienceYears()  { return experienceYears; }
    public String getDegreeLevel()      { return degreeLevel; }
    public String getField()            { return field; }
    public String getBudget()           { return budget.isEmpty() ? "Not specified" : budget; }
    public String getPreferredCountry() { return preferredCountry.isEmpty() ? "Any" : preferredCountry; }
    public String getStudyGap()         { return studyGap; }
    public String getPreferredIntake()  { return preferredIntake; }

    // Setters
    public void setName(String v)             { name = v; }
    public void setGpa(double v)              { gpa = v; }
    public void setIelts(double v)            { ielts = v; }
    public void setExperienceYears(int v)     { experienceYears = v; }
    public void setDegreeLevel(String v)      { degreeLevel = v; }
    public void setField(String v)            { field = v; }
    public void setBudget(String v)           { budget = v; }
    public void setPreferredCountry(String v) { preferredCountry = v; }
    public void setStudyGap(String v)         { studyGap = v; }
    public void setPreferredIntake(String v)  { preferredIntake = v; }
}
