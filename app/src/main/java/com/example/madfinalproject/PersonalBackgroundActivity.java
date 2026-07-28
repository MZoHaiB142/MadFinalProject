package com.example.madfinalproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalBackgroundActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_background);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.lessonIntroduce).setOnClickListener(v -> openLesson("Introduce Yourself"));
        findViewById(R.id.lessonFamily).setOnClickListener(v -> openLesson("Family Background"));
        findViewById(R.id.lessonEducation).setOnClickListener(v -> openLesson("Education History"));
    }

    private void openLesson(String lesson) {
        Toast.makeText(this, lesson + " lesson opened", Toast.LENGTH_SHORT).show();
    }
}
