package com.example.madfinalproject.ai;

import com.example.madfinalproject.ai.models.AIEvaluationRequest;
import com.google.gson.Gson;

public final class PromptBuilder {
    private static final Gson GSON = new Gson();
    private static final String RULES = "Evaluate this visa interview answer fairly and constructively. Assess content, grammar, fluency, confidence, visa intent, keyword coverage, risk, naturalness and professionalism. Do not provide harmful, deceptive, or immigration-rule-evasion advice. Scores must respect the supplied maximum weights. Return only data matching the required JSON schema.";
    private PromptBuilder(){}
    public static String build(AIEvaluationRequest request){return RULES+"\nINPUT_JSON:\n"+GSON.toJson(request);}
}
