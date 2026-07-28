package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdmissionTestsActivity extends AppCompatActivity {
    private TextView selected;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admission_tests);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        selected = findViewById(R.id.filterUndergraduate);
        int[] filters = {R.id.filterUndergraduate, R.id.filterGraduate, R.id.filterMedical, R.id.filterLaw};
        for (int id : filters) findViewById(id).setOnClickListener(v -> select((TextView) v));
        findViewById(R.id.cardSat).setOnClickListener(v ->
                startActivity(new Intent(this, SatDetailActivity.class)));
        findViewById(R.id.cardAct).setOnClickListener(v -> message("ACT selected"));
        findViewById(R.id.cardAp).setOnClickListener(v -> message("AP Exams selected"));
        findViewById(R.id.btnFindTest).setOnClickListener(v -> message("Finding the best admission test for you"));
    }
    private void select(TextView view) {
        selected.setBackgroundResource(R.drawable.bg_test_card);
        selected.setTextColor(0xff52617c);
        selected = view;
        selected.setBackgroundResource(R.drawable.bg_purple_button);
        selected.setTextColor(0xffffffff);
    }
    private void message(String text) { Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }
}
