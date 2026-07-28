package com.example.madfinalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.madfinalproject.foundation.VocabularyChallengeRepository;
import com.example.madfinalproject.foundation.VocabularyWord;
import com.example.madfinalproject.speech.SpeechController;
import com.example.madfinalproject.speech.SpeechListener;

import java.util.Locale;

public class VocabularySpeakingActivity extends VocabularyBaseActivity
        implements SpeechListener {
    private final VocabularyChallengeRepository challengeRepository =
            new VocabularyChallengeRepository();
    private SpeechController speechController;
    private ActivityResultLauncher<String> permissionLauncher;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_speaking);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) speechController.startListening();
                    else showPermissionDialog();
                });
        speechController = new SpeechController(this, this, Locale.US);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnMic).setOnClickListener(view -> toggleListening());
        findViewById(R.id.btnNextSpeaking).setOnClickListener(view -> {
            if (!words.isEmpty()) {
                currentIndex = (currentIndex + 1) % words.size();
                renderWord();
            }
        });
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (!words.isEmpty()) renderWord();
    }

    private void renderWord() {
        VocabularyWord word = words.get(currentIndex);
        ((TextView) findViewById(R.id.speakingWord)).setText(word.word);
        ((TextView) findViewById(R.id.transcriptText))
                .setText("Your recognized speech will appear here.");
        ((TextView) findViewById(R.id.speakingState))
                .setText("Tap the microphone to speak");
        findViewById(R.id.scoreCard).setVisibility(View.GONE);
    }

    private void toggleListening() {
        if (speechController.isListening()) {
            speechController.stopListening();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            speechController.startListening();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Microphone permission")
                    .setMessage("Microphone access is required for pronunciation practice.")
                    .setPositiveButton("Continue",
                            (dialog, which) -> permissionLauncher.launch(
                                    Manifest.permission.RECORD_AUDIO))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Allow microphone access")
                .setMessage("Enable Microphone permission in Settings to use speaking practice.")
                .setPositiveButton("Open Settings", (dialog, which) -> startActivity(
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName()))))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onListeningStarted() {
        ((TextView) findViewById(R.id.speakingState)).setText("Listening...");
        ((TextView) findViewById(R.id.btnMic)).setText("■");
    }

    @Override
    public void onListeningStopped() {
        ((TextView) findViewById(R.id.speakingState)).setText("Processing speech...");
        ((TextView) findViewById(R.id.btnMic)).setText("🎤");
    }

    @Override
    public void onResult(String transcript) {
        ((TextView) findViewById(R.id.transcriptText)).setText(transcript);
        scoreTranscript(transcript);
    }

    @Override
    public void onPartialResult(String transcript) {
        ((TextView) findViewById(R.id.transcriptText)).setText(transcript);
    }

    @Override
    public void onError(String message) {
        ((TextView) findViewById(R.id.speakingState)).setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void scoreTranscript(String transcript) {
        if (words.isEmpty()) return;
        VocabularyWord word = words.get(currentIndex);
        String target = normalize(word.word);
        String spoken = normalize(transcript);
        int score;
        if (spoken.equals(target)) score = 100;
        else if ((" " + spoken + " ").contains(" " + target + " ")) score = 95;
        else {
            int distance = levenshtein(target, spoken);
            int length = Math.max(1, Math.max(target.length(), spoken.length()));
            score = Math.max(0, Math.round((1f - distance / (float) length) * 100));
        }
        findViewById(R.id.scoreCard).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.speakingScore)).setText(score + "%");
        ((TextView) findViewById(R.id.speakingScore)).setTextColor(
                Color.parseColor(score >= 80 ? "#16A34A" : score >= 60 ? "#F59E0B" : "#EF4444"));
        ((TextView) findViewById(R.id.speakingFeedback)).setText(
                score >= 90 ? "Excellent pronunciation. Your word was recognized clearly."
                        : score >= 70 ? "Good attempt. Speak a little more slowly and clearly."
                        : "Try again. Listen to the word and focus on each sound.");
        ((TextView) findViewById(R.id.speakingState)).setText("Tap to practice again");
        vocabularyProgress.recordSpeakingScore(word.id, score);
        challengeRepository.recordSpeaking(score);
    }

    private String normalize(String value) {
        return safe(value).toLowerCase(Locale.US).replaceAll("[^a-z ]", "")
                .replaceAll("\\s+", " ").trim();
    }

    private int levenshtein(String first, String second) {
        int[] previous = new int[second.length() + 1];
        for (int j = 0; j <= second.length(); j++) previous[j] = j;
        for (int i = 1; i <= first.length(); i++) {
            int[] current = new int[second.length() + 1];
            current[0] = i;
            for (int j = 1; j <= second.length(); j++) {
                int cost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost);
            }
            previous = current;
        }
        return previous[second.length()];
    }

    @Override
    protected void onDestroy() {
        if (speechController != null) speechController.destroy();
        challengeRepository.stop();
        super.onDestroy();
    }
}
