package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.madfinalproject.reports.Analytics;
import com.example.madfinalproject.reports.InterviewReport;
import com.example.madfinalproject.reports.InterviewReportService;
import com.example.madfinalproject.reports.ReportStore;
import com.example.madfinalproject.views.VisaProgressChartView;
import com.example.madfinalproject.coach.CoachProfile;
import com.example.madfinalproject.coach.DailyChallengeService;
import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class VisaInterviewProgressActivity extends AppCompatActivity {
    private VisaInterviewQuestion dailyChallenge;
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_visa_interview_progress);findViewById(R.id.btnBack).setOnClickListener(v->finish());load();}
    private void load(){String uid=FirebaseAuth.getInstance().getUid();if(uid==null)uid="guest";String finalUid=uid;FirebaseFirestore.getInstance().collection("userInterviewHistory").whereEqualTo("userId",uid).get().addOnSuccessListener(snapshot->{List<InterviewReport>reports=new ArrayList<>();Gson gson=new Gson();for(DocumentSnapshot d:snapshot.getDocuments())if("report".equals(d.getString("type")))reports.add(gson.fromJson(gson.toJson(d.getData()),InterviewReport.class));reports.sort(Comparator.comparingLong(r->r.interviewDate));render(reports);loadCoach(finalUid,reports);}).addOnFailureListener(e->Toast.makeText(this,"Progress could not be loaded. Cached reports will appear when available.",Toast.LENGTH_LONG).show());}
    private void loadCoach(String uid,List<InterviewReport>reports){FirebaseFirestore.getInstance().collection("userCoach").document(uid).get().addOnSuccessListener(doc->{if(doc.exists()){CoachProfile p=new Gson().fromJson(new Gson().toJson(doc.getData()),CoachProfile.class);set(R.id.coachDashboard,"Readiness: "+p.readinessScore+"% — "+p.readinessLevel+"\nConfidence: "+p.confidenceTrend+" • Grammar: "+p.grammarProgress+"\nAverage answer: "+p.averageAnswerWords+" words • Speaking speed: "+p.speakingSpeedWpm+" WPM\nKeyword coverage: "+p.keywordCoverage+"% • Completeness: "+p.answerCompleteness+"%\n\nPersonalized tips:\n"+android.text.TextUtils.join("\n",p.personalizedTips)+"\n\nSmart revision: "+android.text.TextUtils.join(", ",p.smartRevision)+"\nVocabulary: "+android.text.TextUtils.join(" ",p.vocabularySuggestions));}});if(!reports.isEmpty())new DailyChallengeService().getToday(reports.get(reports.size()-1).country).addOnSuccessListener(q->{dailyChallenge=q;TextView v=findViewById(R.id.coachDashboard);v.append("\n\nDaily challenge (tap when completed):\n"+q.getQuestion());v.setOnClickListener(x->{new DailyChallengeService().markCompleted(q,reports.size());Toast.makeText(this,"Daily challenge completed",Toast.LENGTH_SHORT).show();});});}
    private void render(List<InterviewReport>reports){Analytics a=InterviewReportService.getInstance().calculate(reports);set(R.id.progressScore,a.averageScore+"%");set(R.id.progressSummary,reports.isEmpty()?"Complete your first mock interview.":"Best: "+a.highestScore+"% • Weakest: "+empty(a.weakestCategory));set(R.id.quickStats,"Interviews: "+a.totalInterviews+"     Average: "+a.averageScore+"%\nHighest: "+a.highestScore+"%     Lowest: "+a.lowestScore+"%\nPractice time: "+(a.totalPracticeTimeMillis/60000)+" min     Streak: "+a.streak+"\nBest country: "+empty(a.bestCountry)+"\nMost improved: "+empty(a.mostImprovedCategory));((VisaProgressChartView)findViewById(R.id.progressChart)).setScores(a.weeklyScores);renderHistory(reports);}
    private void renderHistory(List<InterviewReport>reports){TextView view=findViewById(R.id.interviewHistory);if(reports.isEmpty()){view.setText("No interviews yet.");return;}SpannableStringBuilder text=new SpannableStringBuilder();for(int i=reports.size()-1;i>=0;i--){InterviewReport r=reports.get(i);int previous=i>0?reports.get(i-1).overallScore:r.overallScore;int improvement=r.overallScore-previous;int start=text.length();text.append(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(r.interviewDate))).append(" • ").append(r.country).append(" • ").append(String.valueOf(r.overallScore)).append("% • ").append(String.valueOf(r.durationMillis/60000)).append(" min • ").append(improvement>=0?"+":"").append(String.valueOf(improvement)).append("%\n\n");text.setSpan(new ClickableSpan(){@Override public void onClick(@NonNull View widget){ReportStore.set(r);startActivity(new Intent(VisaInterviewProgressActivity.this,VisaInterviewResultActivity.class));}},start,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}view.setText(text);view.setMovementMethod(LinkMovementMethod.getInstance());}
    private void set(int id,String value){((TextView)findViewById(id)).setText(value);}private String empty(String value){return value==null||value.isEmpty()?"Not enough data":value;}
}
