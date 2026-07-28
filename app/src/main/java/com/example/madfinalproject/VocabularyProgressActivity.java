package com.example.madfinalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyUserProgress;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class VocabularyProgressActivity extends VocabularyBaseActivity {
    private final Map<String, VocabularyUserProgress> progress = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_progress);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        vocabularyProgress.listenDetails(new VocabularyProgressRepository.DetailCallback() {
            public void onProgress(Map<String, VocabularyUserProgress> values) {
                progress.clear();
                progress.putAll(values);
                render();
            }
            public void onError(String message) {}
        });
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        render();
    }

    private void render() {
        int known = 0;
        int learning = 0;
        int difficult = 0;
        int progressTotal = 0;
        int attempts = 0;
        int correct = 0;
        Set<String> activeDates = new HashSet<>();
        Map<String, Integer> weekly = new HashMap<>();
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

        for (VocabularyUserProgress value : progress.values()) {
            progressTotal += value.progressPercent;
            if (value.progressPercent >= 100) known++;
            else if (value.progressPercent > 0) learning++;
            if ("difficult".equals(value.status)) difficult++;
            attempts += value.quizAttempts;
            correct += value.quizCorrect;
            if (value.updatedAtMillis > 0) {
                String key = keyFormat.format(new Date(value.updatedAtMillis));
                activeDates.add(key);
                weekly.put(key, weekly.getOrDefault(key, 0) + 1);
            }
        }
        int total = words.size();
        int overall = total == 0 ? 0 : Math.round(progressTotal * 100f / (total * 100f));
        int accuracy = attempts == 0 ? 0 : Math.round(correct * 100f / attempts);
        ((TextView) findViewById(R.id.wordsLearned)).setText(String.valueOf(known));
        ((TextView) findViewById(R.id.overallVocabularyProgress)).setText(overall + "%");
        ((TextView) findViewById(R.id.knownCount)).setText("Known\n" + known);
        ((TextView) findViewById(R.id.learningCount)).setText("Learning\n" + learning);
        ((TextView) findViewById(R.id.difficultCount)).setText("Difficult\n" + difficult);
        ((TextView) findViewById(R.id.dailyStreak))
                .setText("Daily Streak\n" + streak(activeDates, keyFormat) + " Days");
        ((TextView) findViewById(R.id.quizAccuracy)).setText("Quiz Accuracy\n" + accuracy + "%");
        renderWeek(weekly, keyFormat);
    }

    private int streak(Set<String> activeDates, SimpleDateFormat format) {
        Calendar day = Calendar.getInstance();
        int streak = 0;
        while (activeDates.contains(format.format(day.getTime()))) {
            streak++;
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private void renderWeek(Map<String, Integer> weekly, SimpleDateFormat keyFormat) {
        LinearLayout container = findViewById(R.id.weeklyContainer);
        container.removeAllViews();
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, -6);
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.US);
        for (int index = 0; index < 7; index++) {
            String key = keyFormat.format(day.getTime());
            int count = weekly.getOrDefault(key, 0);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            TextView label = new TextView(this);
            label.setText(labelFormat.format(day.getTime()));
            label.setTextColor(Color.parseColor("#52617C"));
            label.setTextSize(10);
            row.addView(label, new LinearLayout.LayoutParams(dp(42), dp(30)));
            ProgressBar bar = new ProgressBar(
                    this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(Math.max(1, words.size()));
            bar.setProgress(count);
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#6D4CEB")));
            row.addView(bar, new LinearLayout.LayoutParams(0, dp(7), 1f));
            TextView value = new TextView(this);
            value.setText("  " + count);
            value.setTextColor(Color.parseColor("#17294D"));
            row.addView(value, new LinearLayout.LayoutParams(dp(34), dp(30)));
            container.addView(row);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
