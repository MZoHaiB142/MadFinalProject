package com.example.madfinalproject;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class VocabularyHomeActivity extends VocabularyBaseActivity {
    private final Map<String, Integer> progressByWord = new LinkedHashMap<>();
    private VocabularyWord todayWord;
    private VocabularyWord continueWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_home);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnBrowseWords).setOnClickListener(view -> openWords(""));
        findViewById(R.id.btnFlashcards).setOnClickListener(
                view -> open(VocabularyFlashcardsActivity.class));
        findViewById(R.id.btnQuiz).setOnClickListener(view -> open(VocabularyQuizActivity.class));
        findViewById(R.id.btnMatch).setOnClickListener(view -> open(VocabularyMatchActivity.class));
        findViewById(R.id.btnFillBlank).setOnClickListener(
                view -> open(VocabularyFillBlankActivity.class));
        findViewById(R.id.btnListening).setOnClickListener(
                view -> open(VocabularyListeningActivity.class));
        findViewById(R.id.btnSpeaking).setOnClickListener(
                view -> open(VocabularySpeakingActivity.class));
        findViewById(R.id.btnBookmarks).setOnClickListener(
                view -> open(VocabularyBookmarksActivity.class));
        findViewById(R.id.btnVocabularyProgress).setOnClickListener(
                view -> open(VocabularyProgressActivity.class));
        findViewById(R.id.btnChallenge).setOnClickListener(
                view -> open(VocabularyChallengeActivity.class));
        findViewById(R.id.btnTodaySpeak).setOnClickListener(
                view -> { if (todayWord != null) speak(todayWord.word); });
        findViewById(R.id.btnLearnToday).setOnClickListener(
                view -> { if (todayWord != null) open(VocabularyDetailActivity.class, todayWord.id); });
        findViewById(R.id.continueCard).setOnClickListener(
                view -> { if (continueWord != null) open(VocabularyDetailActivity.class, continueWord.id); });
        listenForProgress();
        listenForVocabulary();
    }

    private void listenForProgress() {
        vocabularyProgress.listen(new VocabularyProgressRepository.Callback() {
            @Override
            public void onProgress(Map<String, Integer> values) {
                progressByWord.clear();
                progressByWord.putAll(values);
                if (!words.isEmpty()) render();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(
                        VocabularyHomeActivity.this,
                        "Vocabulary progress could not be synced.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    protected void onVocabularyUpdated() {
        render();
    }

    private void render() {
        if (words.isEmpty()) return;
        int learned = 0;
        int totalProgress = 0;
        continueWord = null;
        for (VocabularyWord word : words) {
            int progress = progress(word);
            totalProgress += progress;
            if (progress >= 100) learned++;
            else if (continueWord == null) continueWord = word;
        }
        if (continueWord == null) continueWord = words.get(0);

        int goal = Math.min(10, words.size());
        int goalDone = Math.min(learned, goal);
        ((TextView) findViewById(R.id.homeGoalText))
                .setText(goalDone + " / " + goal + " words learned");
        ProgressBar goalProgress = findViewById(R.id.homeGoalProgress);
        int percent = goal == 0 ? 0 : Math.round(goalDone * 100f / goal);
        ObjectAnimator.ofInt(goalProgress, "progress", goalProgress.getProgress(), percent)
                .setDuration(600);
        goalProgress.setProgress(percent, true);

        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        todayWord = words.get(Math.floorMod(day, words.size()));
        ((TextView) findViewById(R.id.todayWord)).setText(todayWord.word);
        ((TextView) findViewById(R.id.todayMeaning)).setText(todayWord.displayMeaning());

        ((TextView) findViewById(R.id.continueWord))
                .setText(continueWord.word + "\n" + continueWord.displayMeaning());
        ((TextView) findViewById(R.id.continueProgress))
                .setText(progress(continueWord) + "%");
        renderCategories();
    }

    private void renderCategories() {
        LinearLayout container = findViewById(R.id.categoryContainer);
        container.removeAllViews();
        Map<String, Integer> categories = new LinkedHashMap<>();
        for (VocabularyWord word : words) {
            String category = empty(word.category) ? "General" : safe(word.category);
            categories.put(category, categories.getOrDefault(category, 0) + 1);
        }
        for (Map.Entry<String, Integer> value : categories.entrySet()) {
            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(122), dp(64));
            params.setMargins(0, 0, dp(8), 0);
            button.setLayoutParams(params);
            button.setAllCaps(false);
            button.setText(value.getKey() + "\n" + value.getValue() + " words");
            button.setTextColor(Color.parseColor("#5B35DD"));
            button.setTextSize(10);
            button.setBackgroundResource(R.drawable.bg_vocabulary_soft);
            button.setOnClickListener(view -> openWords(value.getKey()));
            container.addView(button);
        }
    }

    private void openWords(String category) {
        startActivity(new Intent(this, VocabularyWordsActivity.class)
                .putExtra("category", category));
    }

    private int progress(VocabularyWord word) {
        Integer value = progressByWord.get(safe(word.id));
        return value == null ? 0 : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
