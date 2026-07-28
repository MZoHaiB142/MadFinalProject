package com.example.madfinalproject.reports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InterviewReport {
    public String interviewId="",userId="",country="",interviewType="Hybrid AI Visa Interview",grade="",aiSummary="";
    public long interviewDate,durationMillis;
    public int questionsAnswered,overallScore,grammarScore,contentScore,fluencyScore,confidenceScore,visaIntentScore,keywordCoverage;
    public boolean riskDetected;
    public List<String> questions=new ArrayList<>(),answers=new ArrayList<>(),feedback=new ArrayList<>(),strongAreas=new ArrayList<>(),weakAreas=new ArrayList<>(),aiSuggestions=new ArrayList<>(),matchedKeywords=new ArrayList<>(),missingKeywords=new ArrayList<>(),riskyStatements=new ArrayList<>(),recommendedPractice=new ArrayList<>(),achievements=new ArrayList<>();
    public Map<String,Integer> categoryScores=new LinkedHashMap<>();
}
