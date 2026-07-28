package com.example.madfinalproject.models;

import com.google.firebase.Timestamp;

public class GeneratedSop {

    private String    userId;
    private String    requestId;
    private String    country;
    private String    sopText;
    private double    score;
    private double    motivationScore;
    private double    visaStrengthScore;
    private double    clarityScore;
    private double    careerAlignmentScore;
    private int       wordCount;
    private String    status; // completed | failed
    private Timestamp createdAt;

    public GeneratedSop() {}

    // Getters
    public String    getUserId()             { return userId; }
    public String    getRequestId()          { return requestId; }
    public String    getCountry()            { return country; }
    public String    getSopText()            { return sopText; }
    public double    getScore()              { return score; }
    public double    getMotivationScore()    { return motivationScore; }
    public double    getVisaStrengthScore()  { return visaStrengthScore; }
    public double    getClarityScore()       { return clarityScore; }
    public double    getCareerAlignmentScore(){ return careerAlignmentScore; }
    public int       getWordCount()          { return wordCount; }
    public String    getStatus()             { return status; }
    public Timestamp getCreatedAt()          { return createdAt; }

    // Setters
    public void setUserId(String v)              { userId = v; }
    public void setRequestId(String v)           { requestId = v; }
    public void setCountry(String v)             { country = v; }
    public void setSopText(String v)             { sopText = v; }
    public void setScore(double v)               { score = v; }
    public void setMotivationScore(double v)     { motivationScore = v; }
    public void setVisaStrengthScore(double v)   { visaStrengthScore = v; }
    public void setClarityScore(double v)        { clarityScore = v; }
    public void setCareerAlignmentScore(double v){ careerAlignmentScore = v; }
    public void setWordCount(int v)              { wordCount = v; }
    public void setStatus(String v)              { status = v; }
    public void setCreatedAt(Timestamp v)        { createdAt = v; }
}