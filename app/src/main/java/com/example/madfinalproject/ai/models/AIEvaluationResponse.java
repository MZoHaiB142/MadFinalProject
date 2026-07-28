package com.example.madfinalproject.ai.models;

import java.util.ArrayList;
import java.util.List;

public final class AIEvaluationResponse {
    private int overallScore;
    private Scores scores;
    private List<String> matchedKeywords, missingKeywords, feedback;
    private boolean riskDetected;
    private String riskReason, improvedAnswer, nextQuestionSuggestion;
    public int getOverallScore(){return overallScore;} public Scores getScores(){return scores;}
    public List<String> getMatchedKeywords(){return safe(matchedKeywords);} public List<String> getMissingKeywords(){return safe(missingKeywords);}
    public List<String> getFeedback(){return safe(feedback);} public boolean isRiskDetected(){return riskDetected;}
    public String getRiskReason(){return text(riskReason);} public String getImprovedAnswer(){return text(improvedAnswer);}
    public String getNextQuestionSuggestion(){return text(nextQuestionSuggestion);}
    public boolean isValid(){return overallScore>=0&&overallScore<=100&&scores!=null&&!getImprovedAnswer().isEmpty();}
    private static List<String> safe(List<String> value){return value==null?new ArrayList<>():value;}
    private static String text(String value){return value==null?"":value;}
}
