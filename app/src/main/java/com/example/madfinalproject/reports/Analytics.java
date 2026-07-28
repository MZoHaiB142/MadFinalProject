package com.example.madfinalproject.reports;

import java.util.ArrayList;
import java.util.List;

public final class Analytics {
    public int averageScore,highestScore,lowestScore,totalInterviews,streak;
    public long totalPracticeTimeMillis;
    public String bestCountry="",weakestCategory="",mostImprovedCategory="";
    public List<Integer> weeklyScores=new ArrayList<>(),monthlyScores=new ArrayList<>();
}
