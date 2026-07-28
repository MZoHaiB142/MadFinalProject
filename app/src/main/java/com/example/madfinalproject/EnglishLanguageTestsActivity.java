package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class EnglishLanguageTestsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_language_tests);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardIelts).setOnClickListener(v ->
                startActivity(new Intent(this, IeltsAcademicActivity.class)));
        findViewById(R.id.cardPte).setOnClickListener(v ->
                startActivity(new Intent(this, PteAcademicActivity.class)));
        View bookmark = findViewById(R.id.btnBookmark);
        bookmark.setOnClickListener(v -> bookmark.setSelected(!bookmark.isSelected()));
    }
}
