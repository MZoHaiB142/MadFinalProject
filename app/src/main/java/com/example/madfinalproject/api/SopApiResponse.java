package com.example.madfinalproject.api;

import com.google.gson.annotations.SerializedName;

// FastAPI se aane wala response
public class SopApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("sop_text")
    private String sopText;

    @SerializedName("score")
    private double score;

    @SerializedName("motivation_score")
    private double motivationScore;

    @SerializedName("visa_strength_score")
    private double visaStrengthScore;

    @SerializedName("clarity_score")
    private double clarityScore;

    @SerializedName("career_alignment_score")
    private double careerAlignmentScore;

    @SerializedName("word_count")
    private int wordCount;

    @SerializedName("error")
    private String error;

    public SopApiResponse() {}

    // Getters
    public boolean isSuccess()              { return success; }
    public String  getSopText()             { return sopText; }
    public double  getScore()               { return score; }
    public double  getMotivationScore()     { return motivationScore; }
    public double  getVisaStrengthScore()   { return visaStrengthScore; }
    public double  getClarityScore()        { return clarityScore; }
    public double  getCareerAlignmentScore(){ return careerAlignmentScore; }
    public int     getWordCount()           { return wordCount; }
    public String  getError()               { return error; }
}