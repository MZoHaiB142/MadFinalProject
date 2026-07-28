package com.example.madfinalproject.ai;

import com.example.madfinalproject.BuildConfig;
import com.example.madfinalproject.ai.models.AIEvaluationRequest;
import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.engine.NextQuestionDecision;
import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.example.madfinalproject.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class AIInterviewService {
    private static final String TAG="AIInterviewService";
    private static volatile AIInterviewService instance;
    private final Gson gson=new Gson();
    private final OpenAIApi api;
    private boolean requestRunning;

    public interface EvaluationCallback { void onSuccess(AIEvaluationResponse result); void onError(String message); }
    public interface NextQuestionCallback { void onSelected(String id,String reason); void onFailure(String message); }

    private AIInterviewService(){
        Interceptor auth=chain->chain.proceed(chain.request().newBuilder()
                .header("Authorization","Bearer "+BuildConfig.OPENAI_API_KEY)
                .header("Content-Type","application/json").build());
        OkHttpClient client=new OkHttpClient.Builder().connectTimeout(60,TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS).writeTimeout(60,TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).addInterceptor(auth).build();
        api=new Retrofit.Builder().baseUrl(BuildConfig.AI_BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build().create(OpenAIApi.class);
    }
    public static AIInterviewService getInstance(){if(instance==null)synchronized(AIInterviewService.class){if(instance==null)instance=new AIInterviewService();}return instance;}

    public synchronized boolean isRequestRunning(){return requestRunning;}
    public void evaluate(AIEvaluationRequest request,EvaluationCallback callback){
        if(BuildConfig.OPENAI_API_KEY.trim().isEmpty()){callback.onError("AI service is not configured. Add OPENAI_API_KEY to local.properties.");return;}
        synchronized(this){if(requestRunning){callback.onError("This answer is already being evaluated.");return;}requestRunning=true;}
        String prompt=PromptBuilder.build(request);
        LogUtils.d(TAG,"Prompt generated for category: "+request.getCategory());
        execute(request,prompt,callback,0);
    }

    private void execute(AIEvaluationRequest original,String prompt,EvaluationCallback callback,int attempt){
        LogUtils.d(TAG,"API request attempt "+(attempt+1));
        api.evaluate(body(prompt)).enqueue(new Callback<OpenAIResponse>(){
            @Override public void onResponse(Call<OpenAIResponse> call, retrofit2.Response<OpenAIResponse> response){
                if(!response.isSuccessful()){
                    if(attempt==0&&response.code()>=500){execute(original,prompt,callback,1);return;}
                    finishError(callback,httpMessage(response.code()));return;
                }
                String json=response.body()==null?"":response.body().content();
                LogUtils.d(TAG,"API response received ("+json.length()+" chars)");
                AIEvaluationResponse result=parse(json);
                if(result==null){
                    if(attempt==0){execute(original,prompt,callback,1);return;}
                    finishError(callback,"The AI returned an invalid response. Please try again.");return;
                }
                save(original,result);
                EvaluationSessionStore.add(original,result);
                synchronized(AIInterviewService.this){requestRunning=false;}
                LogUtils.d(TAG,"Evaluation complete. Score: "+result.getOverallScore());
                callback.onSuccess(result);
            }
            @Override public void onFailure(Call<OpenAIResponse> call,Throwable error){
                LogUtils.e(TAG,"API request failed",error);
                if(attempt==0){execute(original,prompt,callback,1);return;}
                finishError(callback,error instanceof java.net.SocketTimeoutException?"AI evaluation timed out. Please try again.":"No internet connection or AI service unavailable.");
            }
        });
    }

    private AIEvaluationResponse parse(String json){
        try{if(json==null||json.trim().isEmpty())return null;JsonParser.parseString(json).getAsJsonObject();AIEvaluationResponse value=gson.fromJson(json,AIEvaluationResponse.class);LogUtils.d(TAG,"JSON parsing complete");return value!=null&&value.isValid()?value:null;}
        catch(RuntimeException error){LogUtils.e(TAG,"Invalid JSON response",error);return null;}
    }
    private void finishError(EvaluationCallback callback,String message){synchronized(this){requestRunning=false;}callback.onError(message);}

    public void selectNextQuestion(VisaInterviewQuestion current,String answer,AIEvaluationResponse evaluation,
            List<VisaInterviewQuestion> candidates,NextQuestionCallback callback){
        if(BuildConfig.OPENAI_API_KEY.trim().isEmpty()){callback.onFailure("AI service is not configured");return;}
        Map<String,Object> input=new LinkedHashMap<>();input.put("currentQuestionId",current.getId());input.put("currentQuestion",current.getQuestion());input.put("currentCategory",current.getCategory());input.put("userAnswer",answer);input.put("evaluation",evaluation);input.put("interviewProgress",EvaluationSessionStore.results().size());
        java.util.List<Map<String,String>> options=new java.util.ArrayList<>();for(VisaInterviewQuestion q:candidates){Map<String,String> option=new LinkedHashMap<>();option.put("id",q.getId());option.put("category",q.getCategory());option.put("difficulty",q.getDifficulty());option.put("question",q.getQuestion());options.add(option);}input.put("candidateQuestions",options);
        String prompt="Act as a professional visa officer. Select exactly one natural next question from candidateQuestions. Never invent an ID or question. Prefer relevant category transitions and suitable difficulty. Return only schema-valid JSON.\nINPUT_JSON:\n"+gson.toJson(input);
        LogUtils.d(TAG,"Dynamic next-question prompt generated with "+candidates.size()+" candidates");
        api.evaluate(selectionBody(prompt)).enqueue(new Callback<OpenAIResponse>(){
            @Override public void onResponse(Call<OpenAIResponse> call,retrofit2.Response<OpenAIResponse> response){if(!response.isSuccessful()||response.body()==null){callback.onFailure(httpMessage(response.code()));return;}try{String json=response.body().content();JsonParser.parseString(json).getAsJsonObject();NextQuestionDecision value=gson.fromJson(json,NextQuestionDecision.class);if(value==null||value.getNextQuestionId().isEmpty())callback.onFailure("Empty next-question selection");else callback.onSelected(value.getNextQuestionId(),value.getReason());}catch(RuntimeException error){LogUtils.e(TAG,"Malformed next-question response",error);callback.onFailure("Invalid AI selection");}}
            @Override public void onFailure(Call<OpenAIResponse> call,Throwable error){LogUtils.e(TAG,"Next-question request failed",error);callback.onFailure("AI selection unavailable");}
        });
    }
    private String httpMessage(int code){if(code==401)return "AI API key is invalid or missing.";if(code==429)return "AI request limit reached. Please wait and try again.";if(code>=500)return "AI service is temporarily unavailable.";return "AI evaluation failed (error "+code+").";}

    private void save(AIEvaluationRequest request,AIEvaluationResponse result){
        Map<String,Object> data=new HashMap<>();data.put("userId",FirebaseAuth.getInstance().getUid());data.put("country",request.getCountry());data.put("category",request.getCategory());data.put("question",request.getQuestion());data.put("answer",request.getUserAnswer());data.put("evaluation",gson.fromJson(gson.toJson(result),Map.class));data.put("timestamp",FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("userInterviewHistory").add(data)
                .addOnFailureListener(e->LogUtils.e(TAG,"Could not save interview result",e));
    }
    public void saveInterviewSummary(String country,List<String> askedQuestions,List<String> categoryOrder,long durationMillis){List<AIEvaluationResponse> results=EvaluationSessionStore.results();int total=0;java.util.List<Integer>scores=new java.util.ArrayList<>();for(AIEvaluationResponse result:results){total+=result.getOverallScore();scores.add(result.getOverallScore());}Map<String,Object>data=new HashMap<>();data.put("userId",FirebaseAuth.getInstance().getUid());data.put("country",country);data.put("askedQuestions",askedQuestions);data.put("categoryOrder",categoryOrder);data.put("questionScores",scores);data.put("finalScore",results.isEmpty()?0:total/results.size());data.put("interviewDurationMillis",durationMillis);data.put("completed",true);data.put("timestamp",FieldValue.serverTimestamp());FirebaseFirestore.getInstance().collection("userInterviewHistory").add(data).addOnSuccessListener(ref->LogUtils.d(TAG,"Interview summary saved")).addOnFailureListener(e->LogUtils.e(TAG,"Could not save interview summary",e));}

    private Map<String,Object> body(String prompt){Map<String,Object>b=new HashMap<>();b.put("model",BuildConfig.OPENAI_MODEL);b.put("messages",Arrays.asList(message("system","Return only valid JSON matching the supplied schema."),message("user",prompt)));b.put("response_format",responseFormat());return b;}
    private Map<String,Object> selectionBody(String prompt){Map<String,Object>b=new HashMap<>();b.put("model",BuildConfig.OPENAI_MODEL);b.put("messages",Arrays.asList(message("system","Choose only an ID present in candidateQuestions."),message("user",prompt)));Map<String,Object>props=new LinkedHashMap<>();props.put("nextQuestionId",typed("string"));props.put("reason",typed("string"));Map<String,Object>format=new HashMap<>();format.put("type","json_schema");Map<String,Object>js=new HashMap<>();js.put("name","next_interview_question");js.put("strict",true);js.put("schema",object(props));format.put("json_schema",js);b.put("response_format",format);return b;}
    private Map<String,Object> message(String role,String content){Map<String,Object>m=new HashMap<>();m.put("role",role);m.put("content",content);return m;}
    private Map<String,Object> responseFormat(){Map<String,Object>format=new HashMap<>();format.put("type","json_schema");Map<String,Object>js=new HashMap<>();js.put("name","visa_interview_evaluation");js.put("strict",true);js.put("schema",schema());format.put("json_schema",js);return format;}
    private Map<String,Object> schema(){Map<String,Object>p=new LinkedHashMap<>();p.put("overallScore",integer(0,100));Map<String,Object>scoreProps=new LinkedHashMap<>();scoreProps.put("content",integer(0,40));scoreProps.put("grammar",integer(0,20));scoreProps.put("fluency",integer(0,20));scoreProps.put("confidence",integer(0,20));p.put("scores",object(scoreProps));p.put("matchedKeywords",stringArray());p.put("missingKeywords",stringArray());p.put("riskDetected",typed("boolean"));p.put("riskReason",typed("string"));p.put("feedback",stringArray());p.put("improvedAnswer",typed("string"));p.put("nextQuestionSuggestion",typed("string"));return object(p);}
    private Map<String,Object> object(Map<String,Object>props){Map<String,Object>m=new LinkedHashMap<>();m.put("type","object");m.put("properties",props);m.put("required",props.keySet());m.put("additionalProperties",false);return m;}
    private Map<String,Object> typed(String type){return new HashMap<>(Collections.singletonMap("type",type));}
    private Map<String,Object> integer(int min,int max){Map<String,Object>m=typed("integer");m.put("minimum",min);m.put("maximum",max);return m;}
    private Map<String,Object> stringArray(){Map<String,Object>m=typed("array");m.put("items",typed("string"));return m;}
}
