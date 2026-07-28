package com.example.madfinalproject;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyListeningActivity extends VocabularyBaseActivity {
    private int currentIndex;
    private boolean answered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_listening);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnPlayWord).setOnClickListener(view -> playWord());
        findViewById(R.id.btnNextListening).setOnClickListener(view -> {
            currentIndex = (currentIndex + 1) % words.size();
            renderQuestion();
        });
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (!words.isEmpty()) renderQuestion();
    }

    private void renderQuestion() {
        answered = false;
        VocabularyWord current = words.get(currentIndex);
        ((TextView) findViewById(R.id.listeningCounter))
                .setText("Word " + (currentIndex + 1) + " / " + words.size());
        findViewById(R.id.listeningFeedback).setVisibility(android.view.View.GONE);
        Button next = findViewById(R.id.btnNextListening);
        next.setEnabled(false);
        next.setAlpha(0.5f);

        List<VocabularyWord> choices = new ArrayList<>();
        choices.add(current);
        for (VocabularyWord word : words) {
            if (!word.id.equals(current.id) && choices.size() < 4) choices.add(word);
        }
        Collections.shuffle(choices);
        LinearLayout container = findViewById(R.id.listeningOptions);
        container.removeAllViews();
        for (VocabularyWord choice : choices) {
            Button button = optionButton(choice.word);
            button.setOnClickListener(view -> answer(choice, button));
            container.addView(button);
        }
        findViewById(R.id.btnPlayWord).postDelayed(this::playWord, 300);
    }

    private void playWord() {
        if (words.isEmpty()) return;
        speak(words.get(currentIndex).word);
        ProgressBar progress = findViewById(R.id.audioProgress);
        progress.setProgress(0);
        ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0, 100);
        animation.setDuration(1200);
        animation.start();
    }

    private void answer(VocabularyWord choice, Button button) {
        if (answered) return;
        answered = true;
        VocabularyWord current = words.get(currentIndex);
        boolean correct = choice.id.equals(current.id);
        button.setBackground(optionBackground(
                correct ? "#E8F8EE" : "#FFF0F1",
                correct ? "#34B765" : "#EF5350"));
        TextView feedback = findViewById(R.id.listeningFeedback);
        feedback.setVisibility(android.view.View.VISIBLE);
        feedback.setBackgroundResource(correct
                ? R.drawable.bg_vocabulary_success : R.drawable.bg_vocabulary_soft);
        feedback.setText(correct
                ? "Correct! You heard \"" + current.word + "\"."
                : "You heard \"" + current.word + "\". Listen and try again next time.");
        feedback.setTextColor(Color.parseColor(correct ? "#176B36" : "#B42318"));
        vocabularyProgress.save(current.id, correct ? 100 : 60,
                correct ? "listening_correct" : "listening_attempted");
        Button next = findViewById(R.id.btnNextListening);
        next.setEnabled(true);
        next.setAlpha(1f);
    }

    private Button optionButton(String text) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        params.setMargins(0, 0, 0, dp(8));
        button.setLayoutParams(params);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(15), 0, 0, 0);
        button.setText("○     " + text);
        button.setTextColor(Color.parseColor("#17294D"));
        button.setBackground(optionBackground("#FFFFFF", "#E1E6EF"));
        return button;
    }

    private GradientDrawable optionBackground(String fill, String stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(fill));
        drawable.setCornerRadius(dp(11));
        drawable.setStroke(dp(1), Color.parseColor(stroke));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
