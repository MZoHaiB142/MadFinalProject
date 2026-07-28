package com.example.madfinalproject.ai;

import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.ai.models.AIEvaluationRequest;
import java.util.ArrayList;
import java.util.List;

public final class EvaluationSessionStore {
    private static final List<AIEvaluationResponse> RESULTS=new ArrayList<>();
    private static final List<Record> RECORDS=new ArrayList<>();
    private EvaluationSessionStore(){}
    public static synchronized void clear(){RESULTS.clear();RECORDS.clear();}
    public static synchronized void add(AIEvaluationResponse result){RESULTS.add(result);}
    public static synchronized void add(AIEvaluationRequest request,AIEvaluationResponse result){RESULTS.add(result);RECORDS.add(new Record(request,result));}
    public static synchronized List<AIEvaluationResponse> results(){return new ArrayList<>(RESULTS);}
    public static synchronized List<Record> records(){return new ArrayList<>(RECORDS);}
    public static final class Record { public final AIEvaluationRequest request; public final AIEvaluationResponse response; Record(AIEvaluationRequest request,AIEvaluationResponse response){this.request=request;this.response=response;} }
}
