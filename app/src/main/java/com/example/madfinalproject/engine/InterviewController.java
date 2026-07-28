package com.example.madfinalproject.engine;

import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.example.madfinalproject.repository.VisaInterviewRepository;
import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.utils.LogUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class InterviewController {
    private static final String TAG = "InterviewController";
    private static volatile InterviewController instance;
    private final VisaInterviewRepository repository;
    private List<VisaInterviewQuestion> questions = Collections.emptyList();
    private int currentIndex = 0;
    private String country = "";
    private boolean finished;
    private InterviewEngineController hybridController;

    private InterviewController(VisaInterviewRepository repository){this.repository=repository;}
    public static InterviewController getInstance(){if(instance==null)synchronized(InterviewController.class){if(instance==null)instance=new InterviewController(VisaInterviewRepository.getInstance());}return instance;}

    public Task<Void> startInterview(String selectedCountry){
        if(selectedCountry==null||selectedCountry.trim().isEmpty())return Tasks.forException(new IllegalArgumentException("A country is required."));
        String clean=selectedCountry.trim();
        if(clean.equalsIgnoreCase(country)&&!questions.isEmpty()){hybridController=new InterviewEngineController(questions,recent(clean));currentIndex=0;finished=false;LogUtils.d(TAG,"Interview restarted from memory cache for "+country);return Tasks.forResult(null);}
        LogUtils.d(TAG,"Interview started for "+clean);
        return repository.getQuestionsByCountry(clean).continueWith(task->{if(!task.isSuccessful()){LogUtils.e(TAG,"Question download failed",task.getException());throw task.getException();}List<VisaInterviewQuestion> loaded=task.getResult();if(loaded==null||loaded.isEmpty())throw new EmptyInterviewException("No interview questions found for "+clean+".");questions=Collections.unmodifiableList(new ArrayList<>(loaded));hybridController=new InterviewEngineController(questions,recent(clean));country=clean;currentIndex=0;finished=false;LogUtils.d(TAG,"Questions downloaded and cached: "+questions.size());return null;});
    }

    public boolean nextQuestion(){if(questions.isEmpty())return false;if(currentIndex<questions.size()-1){currentIndex++;LogUtils.d(TAG,"Current question index: "+currentIndex);return true;}finishInterview();return false;}
    public boolean previousQuestion(){if(hybridController!=null)return hybridController.previous();if(currentIndex>0){currentIndex--;finished=false;LogUtils.d(TAG,"Current question index: "+currentIndex);return true;}return false;}
    public void finishInterview(){finished=true;QuestionHistoryStore store=QuestionHistoryStore.get();if(store!=null&&hybridController!=null)store.record(country,hybridController.askedIds());LogUtils.d(TAG,"Interview completed for "+country);}
    public void requestNextQuestion(String answer,AIEvaluationResponse evaluation,InterviewEngineController.NextCallback callback){if(hybridController==null){callback.onComplete();return;}hybridController.selectNext(answer,evaluation,callback);}
    public List<String> getAskedQuestionIds(){return hybridController==null?Collections.emptyList():hybridController.askedIds();}
    public List<String> getCategoryOrder(){return hybridController==null?Collections.emptyList():hybridController.categoryOrder();}
    public long getInterviewDurationMillis(){return hybridController==null?0:hybridController.durationMillis();}
    public void resetInterview(){questions=Collections.emptyList();hybridController=null;currentIndex=0;country="";finished=false;LogUtils.d(TAG,"Interview reset");}
    public VisaInterviewQuestion currentQuestion(){return hybridController!=null?hybridController.current():(questions.isEmpty()?null:questions.get(currentIndex));}
    public float currentProgress(){return hybridController==null||questions.isEmpty()?0f:Math.min(1f,hybridController.askedCount()/(float)Math.min(InterviewEngine.MAX_INTERVIEW_QUESTIONS,questions.size()));}
    public int getCurrentQuestionNumber(){return hybridController==null?0:hybridController.askedCount();}
    public int getTotalQuestions(){return Math.min(InterviewEngine.MAX_INTERVIEW_QUESTIONS,questions.size());}
    public int getRemainingQuestions(){return hybridController==null?0:hybridController.remaining();}
    public String getCurrentCategory(){VisaInterviewQuestion q=currentQuestion();return q==null?"":q.getCategory();}
    public String getCountry(){return country;}
    public boolean isInterviewFinished(){return finished;}
    public boolean hasQuestions(){return !questions.isEmpty();}
    private Set<String> recent(String selectedCountry){QuestionHistoryStore store=QuestionHistoryStore.get();return store==null?Collections.emptySet():store.recent(selectedCountry);}

    public static final class EmptyInterviewException extends Exception { public EmptyInterviewException(String message){super(message);} }
}
