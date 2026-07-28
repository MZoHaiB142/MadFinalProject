package com.example.madfinalproject;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestPreparationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_preparation);

        // Find views
        View btnNotification = findViewById(R.id.btnNotification);
        View cardFindTest = findViewById(R.id.cardFindTest);
        View cardEnglishTest = findViewById(R.id.cardEnglishTest);
        View cardAdmissionTest = findViewById(R.id.cardAdmissionTest);
        View cardFoundationTest = findViewById(R.id.cardFoundationTest);
        View cardVisaMockInterview = findViewById(R.id.cardVisaMockInterview);
        View btnStartNow = findViewById(R.id.btnStartNow);

        // Set click listeners
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show());
        }
        if (cardFindTest != null) {
            cardFindTest.setOnClickListener(v -> Toast.makeText(this, "Find Your Required Test clicked", Toast.LENGTH_SHORT).show());
        }
        if (cardEnglishTest != null) {
            cardEnglishTest.setOnClickListener(v ->
                    startActivity(new Intent(this, EnglishLanguageTestsActivity.class)));
        }
        if (cardAdmissionTest != null) {
            cardAdmissionTest.setOnClickListener(v ->
                    startActivity(new Intent(this, AdmissionTestsActivity.class)));
        }
        if (cardFoundationTest != null) {
            cardFoundationTest.setOnClickListener(v ->
                    startActivity(new Intent(this, EnglishFoundationActivity.class)));
        }
        if (cardVisaMockInterview != null) {
            cardVisaMockInterview.setOnClickListener(v ->
                    startActivity(new Intent(this, VisaMockInterviewActivity.class)));
        }
        if (btnStartNow != null) {
            btnStartNow.setOnClickListener(v -> Toast.makeText(this, "Start Now clicked", Toast.LENGTH_SHORT).show());
        }
    }
}
