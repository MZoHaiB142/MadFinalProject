package com.example.madfinalproject;
import android.content.Intent; import android.os.Bundle; import android.widget.Toast; import androidx.appcompat.app.AppCompatActivity;
public class IeltsMockTestsActivity extends AppCompatActivity {
 protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_ielts_mock_tests);((android.widget.TextView)findViewById(R.id.tabMockTests)).setTextColor(0xffe91f32);wire();findViewById(R.id.btnStartMock).setOnClickListener(v->Toast.makeText(this,"Starting new IELTS mock test",Toast.LENGTH_SHORT).show());}
 private void wire(){findViewById(R.id.btnBack).setOnClickListener(v->finish());findViewById(R.id.tabOverview).setOnClickListener(v->open(IeltsPreparationActivity.class));findViewById(R.id.tabLessons).setOnClickListener(v->open(IeltsLessonsActivity.class));findViewById(R.id.tabPractice).setOnClickListener(v->open(IeltsPracticeActivity.class));findViewById(R.id.tabReports).setOnClickListener(v->open(IeltsReportsActivity.class));}
 private void open(Class<?> c){startActivity(new Intent(this,c));finish();}
}
