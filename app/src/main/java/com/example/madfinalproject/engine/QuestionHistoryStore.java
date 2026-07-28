package com.example.madfinalproject.engine;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class QuestionHistoryStore {
    private static final String PREFS="visa_interview_question_history";
    private static final int MAX_RECENT=30;
    private static QuestionHistoryStore instance;
    private final SharedPreferences preferences;
    private QuestionHistoryStore(Context context){preferences=context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public static synchronized void initialize(Context context){if(instance==null)instance=new QuestionHistoryStore(context);}
    public static synchronized QuestionHistoryStore get(){return instance;}
    public Set<String> recent(String country){return new LinkedHashSet<>(preferences.getStringSet(key(country),new LinkedHashSet<>()));}
    public void record(String country,List<String>ids){LinkedHashSet<String>merged=new LinkedHashSet<>(recent(country));for(String id:ids){merged.remove(id);merged.add(id);}List<String>values=new ArrayList<>(merged);if(values.size()>MAX_RECENT)values=values.subList(values.size()-MAX_RECENT,values.size());preferences.edit().putStringSet(key(country),new LinkedHashSet<>(values)).apply();}
    private String key(String country){return "recent_"+(country==null?"":country.trim().toLowerCase(Locale.US).replace(' ','_'));}
}
