package com.example.madfinalproject;
import android.content.Intent; import android.os.Bundle; import androidx.appcompat.app.AppCompatActivity;
public class IeltsLessonsActivity extends AppCompatActivity {
 protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_ielts_lessons); ((android.widget.TextView)findViewById(R.id.tabLessons)).setTextColor(0xffe91f32); wire();}
 private void wire(){findViewById(R.id.btnBack).setOnClickListener(v->finish()); findViewById(R.id.tabOverview).setOnClickListener(v->open(IeltsPreparationActivity.class));findViewById(R.id.tabPractice).setOnClickListener(v->open(IeltsPracticeActivity.class));findViewById(R.id.tabMockTests).setOnClickListener(v->open(IeltsMockTestsActivity.class));findViewById(R.id.tabReports).setOnClickListener(v->open(IeltsReportsActivity.class));}
 private void open(Class<?> c){startActivity(new Intent(this,c));finish();}
}
