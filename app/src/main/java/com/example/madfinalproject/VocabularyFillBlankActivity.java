package com.example.madfinalproject;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class VocabularyFillBlankActivity extends VocabularyBaseActivity {
    private int currentIndex;
    private String selectedAnswer = "";
    private Button selectedButton;
    private boolean checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_fill_blank);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnFillSubmit).setOnClickListener(view -> submitOrNext());
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (!words.isEmpty()) renderQuestion();
    }

    private void renderQuestion() {
        VocabularyWord word = words.get(currentIndex);
        selectedAnswer = "";
        selectedButton = null;
        checked = false;
        ((TextView) findViewById(R.id.fillCounter))
                .setText("Question " + (currentIndex + 1) + " / " + words.size());
        ((ProgressBar) findViewById(R.id.fillProgress))
                .setProgress(Math.round((currentIndex + 1) * 100f / words.size()), true);
        ((TextView) findViewById(R.id.fillSentence)).setText(blankSentence(word));
        findViewById(R.id.fillFeedback).setVisibility(View.GONE);
        Button submit = findViewById(R.id.btnFillSubmit);
        submit.setEnabled(false);
        submit.setAlpha(0.5f);
        submit.setText("Submit");

        List<String> options = new ArrayList<>();
        options.add(word.word);
        for (VocabularyWord candidate : words) {
            if (!candidate.id.equals(word.id) && options.size() < 4) options.add(candidate.word);
        }
        Collections.shuffle(options);
        LinearLayout container = findViewById(R.id.fillOptions);
        container.removeAllViews();
        for (String option : options) {
            Button button = optionButton(option);
            button.setOnClickListener(view -> select(option, button));
            container.addView(button);
        }
    }

    private String blankSentence(VocabularyWord word) {
        String sentence = "";
        if (!word.safe(word.exampleSentences).isEmpty()) {
            sentence = safe(word.exampleSentences.get(0).english);
        }
        if (empty(sentence)) sentence = safe(word.safeSpeakingPractice().sentence);
        if (empty(sentence)) return "The correct word is ______.";
        return sentence.replaceFirst("(?i)" + Pattern.quote(word.word), "________");
    }

    private void select(String answer, Button button) {
        if (checked) return;
        if (selectedButton != null) {
            selectedButton.setBackground(optionBackground("#FFFFFF", "#E1E6EF"));
        }
        selectedAnswer = answer;
        selectedButton = button;
        button.setBackground(optionBackground("#EEE9FF", "#6D4CEB"));
        Button submit = findViewById(R.id.btnFillSubmit);
        submit.setEnabled(true);
        submit.setAlpha(1f);
    }

    private void submitOrNext() {
        if (checked) {
            currentIndex = (currentIndex + 1) % words.size();
            renderQuestion();
            return;
        }
        if (empty(selectedAnswer)) return;
        checked = true;
        VocabularyWord word = words.get(currentIndex);
        boolean correct = selectedAnswer.equalsIgnoreCase(word.word);
        selectedButton.setBackground(optionBackground(
                correct ? "#E8F8EE" : "#FFF0F1",
                correct ? "#34B765" : "#EF5350"));
        TextView feedback = findViewById(R.id.fillFeedback);
        feedback.setVisibility(View.VISIBLE);
        feedback.setBackgroundResource(correct
                ? R.drawable.bg_vocabulary_success : R.drawable.bg_vocabulary_soft);
        feedback.setText(correct
                ? "Correct! \"" + word.word + "\" completes the sentence."
                : "The correct answer is \"" + word.word + "\".");
        feedback.setTextColor(Color.parseColor(correct ? "#176B36" : "#B42318"));
        vocabularyProgress.save(word.id, correct ? 100 : 60,
                correct ? "fill_correct" : "fill_attempted");
        Button submit = findViewById(R.id.btnFillSubmit);
        submit.setText("Next Question");
    }

    private Button optionButton(String text) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        params.setMargins(0, 0, 0, dp(8));
        button.setLayoutParams(params);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(15), 0, dp(12), 0);
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
