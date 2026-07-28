package com.example.madfinalproject;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class PteAcademicActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pte_academic);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFavorite).setOnClickListener(v -> v.setSelected(!v.isSelected()));
        findViewById(R.id.btnStartPreparing).setOnClickListener(v ->
                startActivity(new Intent(this, PtePreparationActivity.class)));
    }
}
