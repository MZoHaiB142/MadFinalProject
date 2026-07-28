package com.example.madfinalproject.recommendations;

import com.example.madfinalproject.models.University;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class UniversityRecommendationEngine {
    private UniversityRecommendationEngine(){}
    public static List<University> rank(List<University> universities,DocumentSnapshot profile){List<University> ranked=new ArrayList<>(universities);if(!isComplete(profile)){Collections.shuffle(ranked);return limit(ranked,5);}String countries=value(profile,"targetCountries","country"),qualification=value(profile,"qualification"),fields=value(profile,"interestedFields","field"),budget=value(profile,"budget");String targetDegree=targetDegree(qualification);for(University university:ranked)university.matchScore=score(university,countries,targetDegree,fields,budget);ranked.sort(Comparator.comparingInt((University u)->u.matchScore).reversed().thenComparing(u->safe(u.ranking)));return limit(ranked,5);}
    private static int score(University u,String countries,String targetDegree,String fields,String budget){int score=Math.max(0,Math.min(20,u.matchScore/5));String searchable=normalize(safe(u.name)+" "+safe(u.location)+" "+join(u.tags));if(!countries.isEmpty()&&containsAny(searchable,countries))score+=25;boolean degreeMatch=false,fieldMatch=false;for(University.Program p:u.getPrograms()){String program=normalize(p.getCourseName()+" "+p.getDegreeLevel());if(!targetDegree.isEmpty()&&program.contains(targetDegree))degreeMatch=true;if(!fields.isEmpty()&&containsAny(program,fields))fieldMatch=true;}if(degreeMatch)score+=25;if(fieldMatch)score+=25;double userBudget=number(budget),fee=number(u.fees);if(userBudget>0&&fee>0){if(fee<=userBudget)score+=15;else if(fee<=userBudget*1.2)score+=7;}if(u.scholarshipCount>0||!u.getScholarships().isEmpty())score+=10;if(!degreeMatch&&targetDegree.isEmpty())score+=10;if(!fieldMatch&&fields.isEmpty())score+=10;return Math.max(1,Math.min(100,score));}
    private static boolean isComplete(DocumentSnapshot p){if(p==null||!p.exists())return false;String q=value(p,"qualification");return !q.isEmpty()&&!normalize(q).contains("select qualification");}
    private static String targetDegree(String qualification){String q=normalize(qualification);if(q.contains("intermediate")||q.contains("a-level"))return "bachelor";if(q.contains("bachelor")||q.contains("bs"))return "master";if(q.contains("master")||q.contains("ms"))return "phd";if(q.contains("matric")||q.contains("o-level"))return "intermediate";return "";}
    private static boolean containsAny(String haystack,String commaValues){for(String part:normalize(commaValues).split("[,;/|]")){String token=part.trim();if(token.length()>2&&haystack.contains(token))return true;}return false;}
    private static double number(String value){if(value==null)return 0;String normalized=normalize(value);String cleaned=normalized.replaceAll("[^0-9.]","");try{if(cleaned.isEmpty())return 0;double parsed=Double.parseDouble(cleaned);if(normalized.matches(".*[0-9]\\s*k([^a-z]|$).*")||normalized.contains("thousand"))parsed*=1000;return parsed;}catch(Exception e){return 0;}}
    private static String value(DocumentSnapshot d,String...keys){if(d==null)return "";for(String key:keys){Object v=d.get(key);if(v!=null&&!String.valueOf(v).trim().isEmpty())return String.valueOf(v);}return "";}
    private static String join(List<String>v){return v==null?"":android.text.TextUtils.join(" ",v);}private static String normalize(String v){return safe(v).toLowerCase(Locale.US);}private static String safe(String v){return v==null?"":v;}private static List<University> limit(List<University>v,int max){return new ArrayList<>(v.subList(0,Math.min(max,v.size())));}
}
