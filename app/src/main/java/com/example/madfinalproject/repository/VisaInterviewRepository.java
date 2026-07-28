package com.example.madfinalproject.repository;

import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.example.madfinalproject.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VisaInterviewRepository {
    private static volatile VisaInterviewRepository instance;
    private final FirebaseFirestore firestore;
    private final Map<String, List<VisaInterviewQuestion>> countryCache = new HashMap<>();

    private VisaInterviewRepository(FirebaseFirestore firestore) { this.firestore = firestore; }
    public static VisaInterviewRepository getInstance() { if (instance == null) synchronized (VisaInterviewRepository.class) { if (instance == null) instance = new VisaInterviewRepository(FirebaseFirestore.getInstance()); } return instance; }

    public Task<List<VisaInterviewQuestion>> getQuestionsByCountry(String country) {
        String key = normalize(country);
        synchronized (countryCache) { if (countryCache.containsKey(key)) return Tasks.forResult(countryCache.get(key)); }
        return firestore.collection(Constants.DB_VISA_INTERVIEW)
                .document(countryDocumentId(country))
                .collection(Constants.DB_VISA_INTERVIEW_QUESTIONS)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    List<VisaInterviewQuestion> questions = mapAndSort(task.getResult().getDocuments());
                    synchronized (countryCache) { countryCache.put(key, questions); }
                    return questions;
                });
    }
    public Task<List<VisaInterviewQuestion>> getQuestionsByCategory(String category) { return query("category", category); }
    public Task<List<VisaInterviewQuestion>> getQuestionsByDifficulty(String difficulty) { return query("difficulty", difficulty); }
    public void clearMemoryCache(){synchronized(countryCache){countryCache.clear();}}

    private Task<List<VisaInterviewQuestion>> query(String field,String value){
        Query query=firestore.collectionGroup(Constants.DB_VISA_INTERVIEW_QUESTIONS).whereEqualTo(field,value);
        return query.get().continueWith(task->{if(!task.isSuccessful())throw task.getException();return mapAndSort(task.getResult().getDocuments());});
    }
    private List<VisaInterviewQuestion> mapAndSort(List<DocumentSnapshot> documents){List<VisaInterviewQuestion> result=new ArrayList<>();for(DocumentSnapshot document:documents){VisaInterviewQuestion question=VisaInterviewQuestion.fromDocument(document);if(!question.getQuestion().isEmpty())result.add(question);}result.sort(Comparator.comparing(VisaInterviewQuestion::getCategory,String.CASE_INSENSITIVE_ORDER).thenComparing(VisaInterviewQuestion::getDifficulty,String.CASE_INSENSITIVE_ORDER).thenComparing(VisaInterviewQuestion::getId,String.CASE_INSENSITIVE_ORDER));return Collections.unmodifiableList(result);}
    private String countryDocumentId(String country){String value=normalize(country);if(value.equals("united states")||value.equals("usa")||value.equals("us"))return "usa";if(value.equals("united kingdom")||value.equals("uk")||value.equals("great britain"))return "uk";return value.replace(" ","").replace("-","");}
    private String normalize(String value){return value==null?"":value.trim().toLowerCase(Locale.US);}
}
