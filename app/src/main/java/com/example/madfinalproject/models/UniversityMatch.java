package com.example.madfinalproject.models;

import java.util.ArrayList;
import java.util.List;

public class UniversityMatch {

    // Firebase fields
    private String  id              = "";
    private String  name            = "";
    private String  country         = "";
    private String  flag            = "";
    private String  countryName     = "";
    private String  program         = "";
    private String  level           = "ms";
    private double  gpaRequired     = 0.0;
    private double  ieltsRequired   = 0.0;
    private int     expRequired     = 0;
    private int     visaRatePakistan = 0;
    private int     acceptanceRate  = 0;
    private String  fees            = "";
    private String  duration        = "";
    private boolean scholarshipAvailable = false;
    private String  aiRecommendation = "";
    private List<String> pros       = new ArrayList<>();
    private List<String> cons       = new ArrayList<>();

    // Computed by AI engine (NOT from Firebase)
    private int     matchScore      = 0;
    private boolean isTopPick       = false;
    private boolean isAiPick        = false;

    // Empty constructor for Firestore
    public UniversityMatch() {}

    // ── Getters ──
    public String  getId()                  { return id; }
    public String  getName()                { return name; }
    public String  getCountry()             { return country; }
    public String  getFlag()                { return flag; }
    public String  getCountryName()         { return countryName; }
    public String  getProgram()             { return program; }
    public String  getLevel()               { return level; }
    public double  getGpaRequired()         { return gpaRequired; }
    public double  getIeltsRequired()       { return ieltsRequired; }
    public int     getExpRequired()         { return expRequired; }
    public int     getVisaRatePakistan()    { return visaRatePakistan; }
    public int     getAcceptanceRate()      { return acceptanceRate; }
    public String  getFees()                { return fees; }
    public String  getDuration()            { return duration; }
    public boolean isScholarshipAvailable() { return scholarshipAvailable; }
    public String  getAiRecommendation()    { return aiRecommendation; }
    public List<String> getPros()           { return pros; }
    public List<String> getCons()           { return cons; }
    public int     getMatchScore()          { return matchScore; }
    public boolean isTopPick()              { return isTopPick; }
    public boolean isAiPick()              { return isAiPick; }

    // ── Setters ──
    public void setId(String id)                              { this.id = id; }
    public void setName(String name)                          { this.name = name; }
    public void setCountry(String country)                    { this.country = country; }
    public void setFlag(String flag)                          { this.flag = flag; }
    public void setCountryName(String countryName)            { this.countryName = countryName; }
    public void setProgram(String program)                    { this.program = program; }
    public void setLevel(String level)                        { this.level = level; }
    public void setGpaRequired(double gpaRequired)            { this.gpaRequired = gpaRequired; }
    public void setIeltsRequired(double ieltsRequired)        { this.ieltsRequired = ieltsRequired; }
    public void setExpRequired(int expRequired)                { this.expRequired = expRequired; }
    public void setVisaRatePakistan(int visaRatePakistan)     { this.visaRatePakistan = visaRatePakistan; }
    public void setAcceptanceRate(int acceptanceRate)          { this.acceptanceRate = acceptanceRate; }
    public void setFees(String fees)                           { this.fees = fees; }
    public void setDuration(String duration)                   { this.duration = duration; }
    public void setScholarshipAvailable(boolean v)             { this.scholarshipAvailable = v; }
    public void setAiRecommendation(String aiRecommendation)  { this.aiRecommendation = aiRecommendation; }
    public void setPros(List<String> pros)                     { this.pros = pros; }
    public void setCons(List<String> cons)                     { this.cons = cons; }
    public void setMatchScore(int matchScore)                  { this.matchScore = matchScore; }
    public void setTopPick(boolean topPick)                    { this.isTopPick = topPick; }
    public void setAiPick(boolean aiPick)                      { this.isAiPick = aiPick; }
}