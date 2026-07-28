package com.example.madfinalproject;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madfinalproject.foundation.VocabularyWord;
import com.example.madfinalproject.foundation.VocabularyChallengeRepository;

import java.util.ArrayList;
import java.util.List;

public class VocabularyQuizActivity extends VocabularyBaseActivity {
    private final VocabularyChallengeRepository challengeRepository =
            new VocabularyChallengeRepository();
    private int currentIndex;
    private int correctCount;
    private boolean answered;
    private String requestedWordId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_quiz);
        requestedWordId = safe(getIntent().getStringExtra("word_id"));
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnNextQuestion).setOnClickListener(view -> nextQuestion());
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (words.isEmpty()) return;
        if (!empty(requestedWordId)) {
            currentIndex = findWord(requestedWordId);
            requestedWordId = "";
        }
        currentIndex = Math.min(currentIndex, words.size() - 1);
        renderQuestion();
    }

    private void renderQuestion() {
        answered = false;
        VocabularyWord word = words.get(currentIndex);
        VocabularyWord.Quiz quiz = word.safeQuiz();
        ((TextView) findViewById(R.id.questionCounter))
                .setText("Question " + (currentIndex + 1) + " / " + words.size());
        ((TextView) findViewById(R.id.quizScore)).setText(correctCount + " correct");
        ((ProgressBar) findViewById(R.id.quizProgress))
                .setProgress(Math.round((currentIndex + 1) * 100f / words.size()), true);
        ((TextView) findViewById(R.id.questionText)).setText(
                empty(quiz.question) ? "What does \"" + word.word + "\" mean?" : quiz.question);
        findViewById(R.id.feedbackText).setVisibility(View.GONE);
        Button next = findViewById(R.id.btnNextQuestion);
        next.setEnabled(false);
        next.setAlpha(0.5f);
        next.setText(currentIndex == words.size() - 1 ? "Finish Quiz" : "Next Question");

        List<String> options = new ArrayList<>(word.safe(quiz.options));
        if (options.isEmpty()) {
            options.add(word.displayMeaning());
            options.add("None of the available meanings");
        }
        LinearLayout container = findViewById(R.id.optionsContainer);
        container.removeAllViews();
        for (int index = 0; index < options.size(); index++) {
            Button option = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(58));
            params.setMargins(0, 0, 0, dp(8));
            option.setLayoutParams(params);
            option.setAllCaps(false);
            option.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            option.setPadding(dp(15), 0, dp(12), 0);
            option.setText(letter(index) + "     " + options.get(index));
            option.setTextColor(Color.parseColor("#17294D"));
            option.setTextSize(12);
            option.setBackground(optionBackground("#FFFFFF", "#E1E6EF"));
            int selected = index;
            option.setOnClickListener(view -> answer(selected, option));
            container.addView(option);
        }
    }

    private void answer(int selected, Button selectedButton) {
        if (answered) return;
        answered = true;
        VocabularyWord word = words.get(currentIndex);
        VocabularyWord.Quiz quiz = word.safeQuiz();
        int correct = Math.max(0, Math.min(quiz.correctAnswer,
                ((LinearLayout) findViewById(R.id.optionsContainer)).getChildCount() - 1));
        boolean isCorrect = selected == correct;
        if (isCorrect) correctCount++;
        LinearLayout container = findViewById(R.id.optionsContainer);
        for (int index = 0; index < container.getChildCount(); index++) {
            View view = container.getChildAt(index);
            view.setEnabled(false);
            if (index == correct) {
                view.setBackground(optionBackground("#E8F8EE", "#34B765"));
            } else if (index == selected) {
                view.setBackground(optionBackground("#FFF0F1", "#EF5350"));
            }
        }
        TextView feedback = findViewById(R.id.feedbackText);
        feedback.setVisibility(View.VISIBLE);
        feedback.setBackgroundResource(isCorrect
                ? R.drawable.bg_vocabulary_success : R.drawable.bg_vocabulary_soft);
        feedback.setText((isCorrect ? "Correct!\n" : "Not quite.\n")
                + (empty(quiz.explanation) ? word.meaning : quiz.explanation));
        feedback.setTextColor(Color.parseColor(isCorrect ? "#176B36" : "#B42318"));
        ((TextView) findViewById(R.id.quizScore)).setText(correctCount + " correct");
        vocabularyProgress.recordQuizResult(word.id, isCorrect);
        Button next = findViewById(R.id.btnNextQuestion);
        next.setEnabled(true);
        next.setAlpha(1f);
    }

    private void nextQuestion() {
        if (!answered) return;
        if (currentIndex >= words.size() - 1) {
            challengeRepository.recordQuiz(true);
            Toast.makeText(this,
                    "Quiz complete: " + correctCount + " / " + words.size(),
                    Toast.LENGTH_LONG).show();
            open(VocabularyMatchActivity.class);
            finish();
        } else {
            currentIndex++;
            renderQuestion();
        }
    }

    private GradientDrawable optionBackground(String fill, String stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(fill));
        drawable.setCornerRadius(dp(11));
        drawable.setStroke(dp(1), Color.parseColor(stroke));
        return drawable;
    }

    private String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        challengeRepository.stop();
        super.onDestroy();
    }
}
