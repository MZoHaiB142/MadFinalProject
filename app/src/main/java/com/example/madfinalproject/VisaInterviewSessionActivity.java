package com.example.madfinalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.madfinalproject.engine.InterviewController;
import com.example.madfinalproject.engine.InterviewEngineController;
import com.example.madfinalproject.ai.AIInterviewService;
import com.example.madfinalproject.ai.models.AIEvaluationRequest;
import com.example.madfinalproject.ai.models.AIEvaluationResponse;
import com.example.madfinalproject.reports.InterviewReport;
import com.example.madfinalproject.reports.InterviewReportService;
import com.example.madfinalproject.coach.BookmarkRepository;
import com.example.madfinalproject.coach.RealTimeCoach;
import com.example.madfinalproject.models.VisaInterviewQuestion;
import com.example.madfinalproject.speech.SpeechController;
import com.example.madfinalproject.speech.SpeechListener;
import com.example.madfinalproject.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class VisaInterviewSessionActivity extends AppCompatActivity implements SpeechListener {
    private static final String TAG = "VisaInterviewSession";
    private static final String PREFS = "speech_permission_prefs";
    private static final String KEY_PERMISSION_REQUESTED = "record_audio_requested";

    private final Map<String, String> answerDrafts = new HashMap<>();
    private InterviewController controller;
    private SpeechController speechController;
    private EditText answerTranscript;
    private TextView speechStatus;
    private TextView microphoneButton;
    private Button nextButton;

    private final ActivityResultLauncher<String> microphonePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                LogUtils.d(TAG, "Microphone permission result: " + granted);
                getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                        .putBoolean(KEY_PERMISSION_REQUESTED, true).apply();
                if (granted) startListening(); else handlePermissionDenied();
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_interview_session);
        controller = InterviewController.getInstance();
        if (!controller.hasQuestions()) {
            new AlertDialog.Builder(this).setTitle("Interview unavailable")
                    .setMessage("Questions have not been loaded. Please start the interview again.")
                    .setPositiveButton("Back", (dialog, which) -> finish())
                    .setCancelable(false).show();
            return;
        }

        answerTranscript = findViewById(R.id.answerTranscript);
        speechStatus = findViewById(R.id.speechStatusText);
        microphoneButton = findViewById(R.id.btnRecordAnswer);
        nextButton = findViewById(R.id.btnNextQuestion);
        speechController = new SpeechController(this, this);
        answerTranscript.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                setNextEnabled(!s.toString().trim().isEmpty());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            saveCurrentDraft();
            speechController.cancelListening();
            if (controller.previousQuestion()) showQuestion(); else finish();
        });
        microphoneButton.setOnClickListener(v -> toggleListening());
        findViewById(R.id.btnClearTranscript).setOnClickListener(v -> clearTranscript());
        findViewById(R.id.questionText).setOnClickListener(v -> saveQuestion(true));
        findViewById(R.id.questionText).setOnLongClickListener(v -> { saveQuestion(false); return true; });
        nextButton.setOnClickListener(v -> moveToNextQuestion());
        showQuestion();
    }

    private void showQuestion() {
        VisaInterviewQuestion question = controller.currentQuestion();
        if (question == null) return;
        ((TextView) findViewById(R.id.questionNumber)).setText(
                "Question " + controller.getCurrentQuestionNumber() + " / " + controller.getTotalQuestions()
                        + "  •  " + controller.getRemainingQuestions() + " remaining");
        ((TextView) findViewById(R.id.questionText)).setText(question.getQuestion());
        ((ProgressBar) findViewById(R.id.interviewProgress)).setProgress(
                Math.round(controller.currentProgress() * 100));
        nextButton.setText(controller.getRemainingQuestions() == 0 ? "Finish Interview" : "Next Question  →");
        String draft = answerDrafts.get(question.getId());
        updateTranscript(draft == null ? "" : draft);
        speechStatus.setText(draft == null || draft.isEmpty()
                ? "Tap the microphone to answer" : "Transcript ready — tap to restart");
    }

    private void moveToNextQuestion() {
        if (answerTranscript.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please record or type an answer first.", Toast.LENGTH_SHORT).show();
            return;
        }
        VisaInterviewQuestion question=controller.currentQuestion();
        String answer=answerTranscript.getText().toString().trim();
        saveCurrentDraft(); speechController.cancelListening();
        android.app.ProgressDialog loading=new android.app.ProgressDialog(this);
        loading.setMessage("Evaluating your answer…");loading.setCancelable(false);loading.show();setNextEnabled(false);
        AIInterviewService.getInstance().evaluate(new AIEvaluationRequest(question,answer),new AIInterviewService.EvaluationCallback(){
            @Override public void onSuccess(AIEvaluationResponse result){runOnUiThread(()->{TextView coach=findViewById(R.id.aiCoachFeedback);coach.setVisibility(android.view.View.VISIBLE);coach.setText("AI Coach\n"+RealTimeCoach.feedback(result));});controller.requestNextQuestion(answer,result,new InterviewEngineController.NextCallback(){
                @Override public void onSelected(VisaInterviewQuestion next){runOnUiThread(()->{if(isFinishing()||isDestroyed())return;loading.dismiss();showQuestion();});}
                @Override public void onComplete(){runOnUiThread(()->{if(isFinishing()||isDestroyed())return;controller.finishInterview();loading.setMessage("Generating your interview report…");InterviewReportService.getInstance().generateAndSave(controller.getCountry(),controller.getInterviewDurationMillis(),new InterviewReportService.Callback(){
                    @Override public void onReady(InterviewReport report){runOnUiThread(()->openResult(loading));}
                    @Override public void onError(String message){runOnUiThread(()->{Toast.makeText(VisaInterviewSessionActivity.this,message,Toast.LENGTH_LONG).show();openResult(loading);});}
                });});}
            });}
            @Override public void onError(String message){runOnUiThread(()->{if(isFinishing()||isDestroyed())return;loading.dismiss();setNextEnabled(true);new AlertDialog.Builder(VisaInterviewSessionActivity.this).setTitle("Evaluation unavailable").setMessage(message).setPositiveButton("Retry",null).show();});}
        });
    }

    private void openResult(android.app.ProgressDialog loading){if(isFinishing()||isDestroyed())return;loading.dismiss();startActivity(new Intent(this,VisaInterviewResultActivity.class));finish();}
    private void saveQuestion(boolean favourite){VisaInterviewQuestion q=controller.currentQuestion();if(q==null)return;BookmarkRepository.getInstance().save(q,favourite);Toast.makeText(this,favourite?"Added to favourites":"Question bookmarked",Toast.LENGTH_SHORT).show();}

    private void toggleListening() {
        if (speechController.isListening()) {
            speechController.stopListening();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this).setTitle("Microphone permission")
                    .setMessage("Microphone access is needed to convert your spoken answer into text.")
                    .setPositiveButton("Continue", (dialog, which) -> requestMicrophonePermission())
                    .setNegativeButton("Not now", null).show();
        } else {
            SharedPreferences preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)) showSettingsDialog();
            else requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
    }

    private void handlePermissionDenied() {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) showSettingsDialog();
        else Toast.makeText(this,
                "Microphone permission was denied. You can still type your answer.", Toast.LENGTH_LONG).show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this).setTitle("Allow microphone access")
                .setMessage("Enable Microphone permission in Settings to use speech-to-text. You can still type answers without it.")
                .setPositiveButton("Open Settings", (dialog, which) -> startActivity(
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName()))))
                .setNegativeButton("Cancel", null).show();
    }

    private void startListening() { speechController.startListening(); }

    private void clearTranscript() {
        speechController.cancelListening();
        updateTranscript("");
        VisaInterviewQuestion question = controller.currentQuestion();
        if (question != null) answerDrafts.remove(question.getId());
        speechStatus.setText("Transcript cleared — tap the microphone to answer");
    }

    private void saveCurrentDraft() {
        VisaInterviewQuestion question = controller.currentQuestion();
        if (question != null) answerDrafts.put(question.getId(), answerTranscript.getText().toString().trim());
    }

    private void updateTranscript(String transcript) {
        answerTranscript.setText(transcript);
        answerTranscript.setSelection(answerTranscript.length());
    }

    private void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        nextButton.setAlpha(enabled ? 1f : 0.55f);
    }

    @Override public void onListeningStarted() {
        speechStatus.setText("Listening… tap microphone to stop");
        microphoneButton.setText("■");
        microphoneButton.setSelected(true);
    }

    @Override public void onListeningStopped() {
        speechStatus.setText("Processing speech…");
        microphoneButton.setText("●");
        microphoneButton.setSelected(false);
    }

    @Override public void onResult(String transcript) {
        updateTranscript(transcript);
        speechStatus.setText("Transcript ready — tap microphone to restart");
    }

    @Override public void onPartialResult(String transcript) {
        updateTranscript(transcript);
        speechStatus.setText("Listening…");
    }

    @Override public void onError(String message) {
        speechStatus.setText("Tap the microphone to try again");
        microphoneButton.setText("●");
        microphoneButton.setSelected(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override protected void onStop() {
        super.onStop();
        if (speechController != null) speechController.cancelListening();
    }

    @Override protected void onDestroy() {
        if (speechController != null) speechController.destroy();
        super.onDestroy();
    }
}
