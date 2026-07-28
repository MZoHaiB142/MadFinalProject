package com.example.madfinalproject;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import com.example.madfinalproject.engine.InterviewController;
import com.example.madfinalproject.engine.QuestionHistoryStore;
import com.example.madfinalproject.ai.EvaluationSessionStore;
import com.example.madfinalproject.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestoreException;
import androidx.appcompat.app.AppCompatActivity;

public class VisaInterviewDashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_interview_dashboard);
        QuestionHistoryStore.initialize(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        String country = getIntent().getStringExtra(Constants.EXTRA_INTERVIEW_COUNTRY);
        if (country != null) ((TextView) findViewById(R.id.selectedCountry)).setText(country + " preparation");
        findViewById(R.id.btnMyProgress).setOnClickListener(v ->
                startActivity(new Intent(this, VisaInterviewProgressActivity.class)));
        findViewById(R.id.topicPersonalBackground).setOnClickListener(v ->
                startActivity(new Intent(this, PersonalBackgroundActivity.class)
                        .putExtra("country", country)));
        findViewById(R.id.topicStudyPlan).setOnClickListener(v -> openTopic("study_plan"));
        findViewById(R.id.topicFinancialSituation).setOnClickListener(v -> openTopic("financial"));
        findViewById(R.id.topicHomeTies).setOnClickListener(v -> openTopic("home_ties"));
        findViewById(R.id.topicVisaPurpose).setOnClickListener(v -> openTopic("visa_purpose"));
        findViewById(R.id.btnStartMockInterview).setOnClickListener(v -> loadAndStart(country));
    }

    private void openTopic(String topic) {
        startActivity(new Intent(this, VisaTopicDetailActivity.class).putExtra("topic", topic));
    }

    private void loadAndStart(String country) {
        EvaluationSessionStore.clear();
        ProgressDialog loading = new ProgressDialog(this);
        loading.setMessage("Downloading interview questions…");
        loading.setCancelable(false);
        loading.show();
        InterviewController.getInstance().startInterview(country)
                .addOnSuccessListener(unused -> {
                    if (isFinishing() || isDestroyed()) return;
                    loading.dismiss();
                    startActivity(new Intent(this, VisaInterviewSessionActivity.class));
                })
                .addOnFailureListener(error -> {
                    if (isFinishing() || isDestroyed()) return;
                    loading.dismiss();
                    showLoadError(country, error);
                });
    }

    private void showLoadError(String country, Exception error) {
        String message;
        if (error instanceof InterviewController.EmptyInterviewException) {
            message = "No interview questions are available for " + country + ".";
        } else if (error instanceof FirebaseFirestoreException
                && ((FirebaseFirestoreException) error).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            message = "Firestore permission denied. Please check the visaInterviewQuestions security rules.";
        } else {
            message = "Questions could not be downloaded. Check your internet connection and try again.";
        }
        new AlertDialog.Builder(this).setTitle("Unable to start interview").setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Retry", (dialog, which) -> loadAndStart(country)).show();
    }
}
