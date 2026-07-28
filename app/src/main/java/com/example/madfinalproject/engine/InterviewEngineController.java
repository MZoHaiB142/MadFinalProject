package com.example.madfinalproject.engine;

import com.example.madfinalproject.ai.AIInterviewService;
import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.example.madfinalproject.utils.LogUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class InterviewEngineController {
    private static final String TAG="HybridInterview";
    private final InterviewEngine engine=new InterviewEngine();
    private final List<VisaInterviewQuestion> all;
    private final Set<String> asked=new LinkedHashSet<>();
    private final Set<String> recent;
    private final List<VisaInterviewQuestion> history=new ArrayList<>();
    private VisaInterviewQuestion current;
    private final long startedAt=System.currentTimeMillis();

    public interface NextCallback { void onSelected(VisaInterviewQuestion question); void onComplete(); }
    public InterviewEngineController(List<VisaInterviewQuestion> questions,Set<String> recentQuestions){all=new ArrayList<>(questions);recent=new LinkedHashSet<>(recentQuestions);current=chooseOpeningQuestion();if(current!=null){asked.add(current.getId());history.add(current);}}
    public VisaInterviewQuestion current(){return current;} public int askedCount(){return asked.size();}
    public int remaining(){return Math.max(0,Math.min(InterviewEngine.MAX_INTERVIEW_QUESTIONS,all.size())-asked.size());}
    public boolean complete(){return current==null||asked.size()>=InterviewEngine.MAX_INTERVIEW_QUESTIONS||asked.size()>=all.size()||requiredCategoriesComplete();}
    public List<String> askedIds(){return new ArrayList<>(asked);}
    public List<String> categoryOrder(){List<String> values=new ArrayList<>();for(VisaInterviewQuestion q:history)if(values.isEmpty()||!values.get(values.size()-1).equalsIgnoreCase(q.getCategory()))values.add(q.getCategory());return values;}
    public long durationMillis(){return System.currentTimeMillis()-startedAt;}
    private boolean requiredCategoriesComplete(){Set<String>required=new java.util.HashSet<>(),completed=new java.util.HashSet<>();for(VisaInterviewQuestion q:all){String category=q.getCategory().trim().toLowerCase(java.util.Locale.US);required.add(category);if(asked.contains(q.getId()))completed.add(category);}return !required.isEmpty()&&completed.containsAll(required);}
    public boolean previous(){if(history.size()<2)return false;history.remove(history.size()-1);current=history.get(history.size()-1);return true;}
    public void selectNext(String answer,AIEvaluationResponse evaluation,NextCallback callback){
        if(complete()){LogUtils.d(TAG,"Interview completed");callback.onComplete();return;}
        List<VisaInterviewQuestion> candidates=engine.candidates(all,asked,recent,current,answer,evaluation);
        if(candidates.isEmpty()){callback.onComplete();return;}
        String oldCategory=current.getCategory();String difficulty=engine.difficulty(evaluation.getOverallScore());LogUtils.d(TAG,"Difficulty changed/selected: "+difficulty);
        AIInterviewService.getInstance().selectNextQuestion(current,answer,evaluation,candidates,new AIInterviewService.NextQuestionCallback(){
            @Override public void onSelected(String id,String reason){VisaInterviewQuestion selected=find(candidates,id);if(selected==null){useFallback(candidates,callback,"AI returned an unknown ID");return;}apply(selected,oldCategory,reason,callback);}
            @Override public void onFailure(String message){useFallback(candidates,callback,message);}
        });
    }
    private void useFallback(List<VisaInterviewQuestion> candidates,NextCallback callback,String reason){LogUtils.w("Hybrid selection fallback: "+reason);VisaInterviewQuestion selected=engine.fallback(candidates);if(selected==null)callback.onComplete();else apply(selected,current.getCategory(),"Rule-based fallback",callback);}
    private void apply(VisaInterviewQuestion selected,String oldCategory,String reason,NextCallback callback){current=selected;asked.add(selected.getId());history.add(selected);LogUtils.d(TAG,"Question selected: "+selected.getId()+" - "+reason);if(!oldCategory.equalsIgnoreCase(selected.getCategory()))LogUtils.d(TAG,"Category changed: "+oldCategory+" -> "+selected.getCategory());callback.onSelected(selected);}
    private VisaInterviewQuestion find(List<VisaInterviewQuestion> values,String id){for(VisaInterviewQuestion q:values)if(q.getId().equalsIgnoreCase(id))return q;return null;}
    private VisaInterviewQuestion chooseOpeningQuestion(){if(all.isEmpty())return null;List<VisaInterviewQuestion>fresh=new ArrayList<>(),fallback=new ArrayList<>();for(VisaInterviewQuestion q:all){String c=q.getCategory().toLowerCase(java.util.Locale.US);if(c.contains("greeting")||c.contains("personal")||c.contains("introduction")){fallback.add(q);if(!recent.contains(q.getId()))fresh.add(q);}}List<VisaInterviewQuestion>pool=!fresh.isEmpty()?fresh:!fallback.isEmpty()?fallback:all;return pool.get(new java.util.Random(System.nanoTime()).nextInt(pool.size()));}
}
