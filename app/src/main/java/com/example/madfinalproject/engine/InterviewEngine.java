package com.example.madfinalproject.engine;

import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.models.VisaInterviewQuestion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class InterviewEngine {
    public static final int MAX_INTERVIEW_QUESTIONS=12;
    private static final int MAX_CANDIDATES=12;
    public static final int DEFAULT_MIN_PER_CATEGORY=1;
    public static final int DEFAULT_MAX_PER_CATEGORY=3;

    public List<VisaInterviewQuestion> candidates(List<VisaInterviewQuestion> all,Set<String> asked,
            Set<String> recent,VisaInterviewQuestion current,String answer,AIEvaluationResponse evaluation){
        String preferredCategory=preferredCategory(current,answer,evaluation);
        String difficulty=difficulty(evaluation.getOverallScore());
        List<VisaInterviewQuestion> remaining=new ArrayList<>();
        for(VisaInterviewQuestion q:all)if(!asked.contains(q.getId())&&askedInCategory(all,asked,q.getCategory())<DEFAULT_MAX_PER_CATEGORY)remaining.add(q);
        if(remaining.isEmpty())for(VisaInterviewQuestion q:all)if(!asked.contains(q.getId()))remaining.add(q);
        java.util.Collections.shuffle(remaining);
        remaining.sort(Comparator
                .comparingInt((VisaInterviewQuestion q)->recent.contains(q.getId())?1:0)
                .thenComparingInt((VisaInterviewQuestion q)->categoryRank(q,preferredCategory))
                .thenComparingInt(q->difficultyRank(q.getDifficulty(),difficulty))
                .thenComparing(VisaInterviewQuestion::getId,String.CASE_INSENSITIVE_ORDER));
        return new ArrayList<>(remaining.subList(0,Math.min(MAX_CANDIDATES,remaining.size())));
    }

    public VisaInterviewQuestion fallback(List<VisaInterviewQuestion> candidates){return candidates.isEmpty()?null:candidates.get(0);}
    public String difficulty(int score){return score<50?"easy":score<=80?"medium":"hard";}
    private String preferredCategory(VisaInterviewQuestion current,String answer,AIEvaluationResponse result){String text=normalize(answer);if(text.contains("university")||text.contains("college"))return "university";if(text.contains("scholarship"))return "scholarship";if(text.contains("sponsor")||text.contains("father")||text.contains("mother"))return "finance";if(text.contains("research")||text.contains("course")||text.contains("program"))return "course";if(text.contains("work experience")||text.contains("employment")||text.contains("job"))return "employment";return result.getOverallScore()<50?normalize(current.getCategory()):nextCategory(current.getCategory());}
    private String nextCategory(String current){String[] order={"greeting","personal","education","country","university","course","finance","family","career","future","documents","closing"};String value=normalize(current);for(int i=0;i<order.length-1;i++)if(value.contains(order[i]))return order[i+1];return value;}
    private int categoryRank(VisaInterviewQuestion q,String preferred){String c=normalize(q.getCategory());return c.contains(preferred)?0:1;}
    private int difficultyRank(String actual,String desired){String a=normalize(actual);if(a.equals(desired))return 0;if(desired.equals("medium"))return 1;return a.equals("medium")?1:2;}
    private int askedInCategory(List<VisaInterviewQuestion> all,Set<String> asked,String category){int count=0;for(VisaInterviewQuestion q:all)if(asked.contains(q.getId())&&normalize(q.getCategory()).equals(normalize(category)))count++;return count;}
    private String normalize(String value){return value==null?"":value.trim().toLowerCase(Locale.US).replace('_',' ');}
}
