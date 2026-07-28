package com.example.madfinalproject.models;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class VisaInterviewQuestion {
    private final String id;
    private final String country;
    private final String category;
    private final String difficulty;
    private final String question;
    private final List<String> expectedKeywords;
    private final List<String> avoidKeywords;
    private final SampleAnswer sampleAnswer;
    private final List<String> tips;
    private final ScoreWeights scoreWeights;

    public VisaInterviewQuestion(String id, String country, String category, String difficulty,
                                 String question, List<String> expectedKeywords,
                                 List<String> avoidKeywords, SampleAnswer sampleAnswer,
                                 List<String> tips, ScoreWeights scoreWeights) {
        this.id = safe(id);
        this.country = safe(country);
        this.category = safe(category);
        this.difficulty = safe(difficulty);
        this.question = safe(question);
        this.expectedKeywords = immutable(expectedKeywords);
        this.avoidKeywords = immutable(avoidKeywords);
        this.sampleAnswer = sampleAnswer == null ? SampleAnswer.empty() : sampleAnswer;
        this.tips = immutable(tips);
        this.scoreWeights = scoreWeights == null ? ScoreWeights.defaultWeights() : scoreWeights;
    }

    public static VisaInterviewQuestion fromDocument(DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        if (data == null) data = Collections.emptyMap();
        String storedId = string(data.get("id"));
        return fromMap(storedId.isEmpty() ? document.getId() : storedId, data);
    }

    public static VisaInterviewQuestion fromMap(String fallbackId, Map<String, Object> data) {
        String storedId = string(data.get("id"));
        return new VisaInterviewQuestion(
                storedId.isEmpty() ? fallbackId : storedId,
                string(data.get("country")), string(data.get("category")),
                string(data.get("difficulty")), string(data.get("question")),
                strings(data.get("expectedKeywords")), strings(data.get("avoidKeywords")),
                SampleAnswer.from(data.get("sampleAnswer")), strings(data.get("tips")),
                ScoreWeights.from(data.get("scoreWeights")));
    }

    public static VisaInterviewQuestion fromJson(Map<String, Object> data) {
        return fromMap("", data == null ? Collections.emptyMap() : data);
    }

    public Map<String, Object> toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id); map.put("country", country); map.put("category", category);
        map.put("difficulty", difficulty); map.put("question", question);
        map.put("expectedKeywords", expectedKeywords); map.put("avoidKeywords", avoidKeywords);
        map.put("sampleAnswer", sampleAnswer.toJson()); map.put("tips", tips);
        map.put("scoreWeights", scoreWeights.toJson());
        return map;
    }

    public VisaInterviewQuestion copyWith(String id, String country, String category,
            String difficulty, String question, List<String> expectedKeywords,
            List<String> avoidKeywords, SampleAnswer sampleAnswer, List<String> tips,
            ScoreWeights scoreWeights) {
        return new VisaInterviewQuestion(id == null ? this.id : id,
                country == null ? this.country : country,
                category == null ? this.category : category,
                difficulty == null ? this.difficulty : difficulty,
                question == null ? this.question : question,
                expectedKeywords == null ? this.expectedKeywords : expectedKeywords,
                avoidKeywords == null ? this.avoidKeywords : avoidKeywords,
                sampleAnswer == null ? this.sampleAnswer : sampleAnswer,
                tips == null ? this.tips : tips,
                scoreWeights == null ? this.scoreWeights : scoreWeights);
    }

    public String getId(){return id;} public String getCountry(){return country;}
    public String getCategory(){return category;} public String getDifficulty(){return difficulty;}
    public String getQuestion(){return question;} public List<String> getExpectedKeywords(){return expectedKeywords;}
    public List<String> getAvoidKeywords(){return avoidKeywords;} public SampleAnswer getSampleAnswer(){return sampleAnswer;}
    public List<String> getTips(){return tips;} public ScoreWeights getScoreWeights(){return scoreWeights;}

    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof VisaInterviewQuestion))return false;VisaInterviewQuestion q=(VisaInterviewQuestion)o;return id.equals(q.id)&&country.equals(q.country)&&category.equals(q.category)&&difficulty.equals(q.difficulty)&&question.equals(q.question)&&expectedKeywords.equals(q.expectedKeywords)&&avoidKeywords.equals(q.avoidKeywords)&&sampleAnswer.equals(q.sampleAnswer)&&tips.equals(q.tips)&&scoreWeights.equals(q.scoreWeights);}
    @Override public int hashCode(){return Objects.hash(id,country,category,difficulty,question,expectedKeywords,avoidKeywords,sampleAnswer,tips,scoreWeights);}

    private static String safe(String value){return value == null ? "" : value.trim();}
    private static String string(Object value){return value == null ? "" : String.valueOf(value).trim();}
    private static List<String> immutable(List<String> values){return Collections.unmodifiableList(values == null ? new ArrayList<>() : new ArrayList<>(values));}
    private static List<String> strings(Object value){List<String> result=new ArrayList<>();if(value instanceof List)for(Object item:(List<?>)value)if(item!=null)result.add(String.valueOf(item));return result;}

    public static final class SampleAnswer {
        private final String shortAnswer; private final String ideal; private final List<String> keyPoints;
        public SampleAnswer(String shortAnswer,String ideal,List<String> keyPoints){this.shortAnswer=safe(shortAnswer);this.ideal=safe(ideal);this.keyPoints=immutable(keyPoints);}
        static SampleAnswer empty(){return new SampleAnswer("","",Collections.emptyList());}
        static SampleAnswer from(Object value){if(value instanceof Map){Map<?,?>m=(Map<?,?>)value;return new SampleAnswer(string(m.get("short")),string(m.get("ideal")),strings(m.get("keyPoints")));}if(value instanceof String)return new SampleAnswer((String)value,(String)value,Collections.emptyList());return empty();}
        public Map<String,Object> toJson(){Map<String,Object>m=new HashMap<>();m.put("short",shortAnswer);m.put("ideal",ideal);m.put("keyPoints",keyPoints);return m;}
        public String getShortAnswer(){return shortAnswer;} public String getIdeal(){return ideal;} public List<String> getKeyPoints(){return keyPoints;}
        @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof SampleAnswer))return false;SampleAnswer s=(SampleAnswer)o;return shortAnswer.equals(s.shortAnswer)&&ideal.equals(s.ideal)&&keyPoints.equals(s.keyPoints);}@Override public int hashCode(){return Objects.hash(shortAnswer,ideal,keyPoints);}
    }

    public static final class ScoreWeights {
        private final int content, grammar, fluency, confidence;
        public ScoreWeights(int content,int grammar,int fluency,int confidence){this.content=content;this.grammar=grammar;this.fluency=fluency;this.confidence=confidence;}
        static ScoreWeights defaultWeights(){return new ScoreWeights(40,20,20,20);}
        static ScoreWeights from(Object value){if(!(value instanceof Map))return defaultWeights();Map<?,?>m=(Map<?,?>)value;return new ScoreWeights(number(m.get("content"),40),number(m.get("grammar"),20),number(m.get("fluency"),20),number(m.get("confidence"),20));}
        public Map<String,Object> toJson(){Map<String,Object>m=new HashMap<>();m.put("content",content);m.put("grammar",grammar);m.put("fluency",fluency);m.put("confidence",confidence);return m;}
        public int getContent(){return content;}public int getGrammar(){return grammar;}public int getFluency(){return fluency;}public int getConfidence(){return confidence;}
        private static int number(Object v,int fallback){return v instanceof Number?((Number)v).intValue():fallback;}
        @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof ScoreWeights))return false;ScoreWeights s=(ScoreWeights)o;return content==s.content&&grammar==s.grammar&&fluency==s.fluency&&confidence==s.confidence;}@Override public int hashCode(){return Objects.hash(content,grammar,fluency,confidence);}
    }
}
