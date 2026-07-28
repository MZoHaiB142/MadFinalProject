package com.example.madfinalproject.ai.models;

import com.example.madfinalproject.models.VisaInterviewQuestion;
import java.util.List;

public final class AIEvaluationRequest {
    private final String country, category, question, userAnswer;
    private final List<String> expectedKeywords, avoidKeywords;
    private final VisaInterviewQuestion.SampleAnswer sampleAnswer;
    private final VisaInterviewQuestion.ScoreWeights scoreWeights;
    public AIEvaluationRequest(VisaInterviewQuestion q,String answer){country=q.getCountry();category=q.getCategory();question=q.getQuestion();userAnswer=answer;expectedKeywords=q.getExpectedKeywords();avoidKeywords=q.getAvoidKeywords();sampleAnswer=q.getSampleAnswer();scoreWeights=q.getScoreWeights();}
    public String getCountry(){return country;} public String getCategory(){return category;} public String getQuestion(){return question;} public String getUserAnswer(){return userAnswer;}
}
