package com.example.madfinalproject;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class IeltsPreparationActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ielts_preparation);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnContinue).setOnClickListener(v ->
                Toast.makeText(this, "Continuing Listening Section 1", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnEditScore).setOnClickListener(v ->
                Toast.makeText(this, "Edit target band score", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tabLessons).setOnClickListener(v -> startActivity(new Intent(this, IeltsLessonsActivity.class)));
        findViewById(R.id.tabPractice).setOnClickListener(v -> startActivity(new Intent(this, IeltsPracticeActivity.class)));
        findViewById(R.id.tabMockTests).setOnClickListener(v -> startActivity(new Intent(this, IeltsMockTestsActivity.class)));
        findViewById(R.id.tabReports).setOnClickListener(v -> startActivity(new Intent(this, IeltsReportsActivity.class)));
    }
}
