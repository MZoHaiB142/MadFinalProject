package com.example.madfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VisaTopicDetailActivity extends AppCompatActivity {
    private TextView title, topicName, questionCount, percent, tip;
    private final TextView[] lessonTitles = new TextView[5];
    private final TextView[] lessonMeta = new TextView[5];

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_topic_detail);
        bind();
        render(getIntent().getStringExtra("topic"));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        for (int i = 0; i < 3; i++) {
            final int index = i;
            findViewById(getResources().getIdentifier("lesson" + (i + 1), "id", getPackageName()))
                    .setOnClickListener(v -> Toast.makeText(this, lessonTitles[index].getText() + " opened", Toast.LENGTH_SHORT).show());
        }
    }

    private void bind() {
        title=findViewById(R.id.topicTitle); topicName=findViewById(R.id.topicName); questionCount=findViewById(R.id.topicQuestions); percent=findViewById(R.id.topicPercent); tip=findViewById(R.id.topicTip);
        int[] titleIds={R.id.lessonTitle1,R.id.lessonTitle2,R.id.lessonTitle3,R.id.lessonTitle4,R.id.lessonTitle5};
        int[] metaIds={R.id.lessonMeta1,R.id.lessonMeta2,R.id.lessonMeta3,R.id.lessonMeta4,R.id.lessonMeta5};
        for(int i=0;i<5;i++){lessonTitles[i]=findViewById(titleIds[i]);lessonMeta[i]=findViewById(metaIds[i]);}
    }

    private void render(String topic) {
        String name, questions, progress, advice; String[] lessons; String[] meta;
        if("financial".equals(topic)){name="Financial Situation";questions="20 Questions";progress="60%";advice="Keep financial documents clear, consistent and ready to explain.";lessons=new String[]{"Source of Funds","Sponsor Details","Tuition & Living Costs","Bank Statements","Financial Backup Plan"};meta=new String[]{"5 Questions     10 min","4 Questions       8 min","4 Questions     10 min","4 Questions       8 min","3 Questions       6 min"};}
        else if("home_ties".equals(topic)){name="Ties to Home Country";questions="18 Questions";progress="40%";advice="Explain genuine family, career and community ties with specific examples.";lessons=new String[]{"Family Connections","Career Commitments","Property & Assets","Community Ties","Return Plan"};meta=new String[]{"4 Questions       8 min","4 Questions     10 min","3 Questions       7 min","3 Questions       6 min","4 Questions     10 min"};}
        else if("visa_purpose".equals(topic)){name="Visa Purpose & Intention";questions="16 Questions";progress="70%";advice="Keep your purpose honest and align every answer with your study goals.";lessons=new String[]{"Purpose of Travel","Why This Country?","Study Intentions","Visa Compliance","Future Goals"};meta=new String[]{"4 Questions       8 min","3 Questions       7 min","3 Questions       8 min","3 Questions       6 min","3 Questions       7 min"};}
        else{name="Study Plan";questions="28 Questions";progress="50%";advice="Connect your course choice with your education, skills and long-term career plan.";lessons=new String[]{"Why This Course?","Why This University?","Course Structure","Academic Preparation","Career Plan"};meta=new String[]{"6 Questions     10 min","6 Questions     10 min","5 Questions       8 min","5 Questions       8 min","6 Questions     10 min"};}
        title.setText(name);topicName.setText(name);questionCount.setText(questions);percent.setText("◯\n"+progress);tip.setText("Tip\n"+advice);
        for(int i=0;i<5;i++){lessonTitles[i].setText(lessons[i]);lessonMeta[i].setText(meta[i]);}
    }
}
