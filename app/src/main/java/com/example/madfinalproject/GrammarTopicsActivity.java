package com.example.madfinalproject;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.foundation.GrammarCurriculumMeta;
import com.example.madfinalproject.foundation.GrammarLesson;
import com.example.madfinalproject.foundation.GrammarProgressRepository;
import com.example.madfinalproject.foundation.GrammarRepository;
import com.example.madfinalproject.views.LessonProgressView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarTopicsActivity extends AppCompatActivity {
    private final GrammarRepository repository = new GrammarRepository();
    private final GrammarProgressRepository progressRepository = new GrammarProgressRepository();
    private final List<GrammarLesson> lessons = new ArrayList<>();
    private final Map<String, Integer> progressByLesson = new HashMap<>();
    private LinearLayout topicContainer;
    private ProgressBar loading;
    private TextView empty;
    private GrammarCurriculumMeta curriculumMeta = new GrammarCurriculumMeta();
    private boolean lessonsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_topics);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        topicContainer = findViewById(R.id.topicContainer);
        loading = findViewById(R.id.topicLoading);
        empty = findViewById(R.id.topicEmpty);
        listenForGrammar();
        listenForProgress();
    }

    private void listenForGrammar() {
        showLoading(true);
        repository.listen(new GrammarRepository.Callback() {
            @Override
            public void onData(GrammarCurriculumMeta meta, List<GrammarLesson> values) {
                curriculumMeta = meta;
                lessons.clear();
                lessons.addAll(values);
                lessonsLoaded = true;
                showLoading(false);
                renderMeta();
                renderLessons(lessons);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                topicContainer.removeAllViews();
                empty.setVisibility(View.VISIBLE);
                empty.setText("Unable to load grammar lessons.\n" + message);
            }
        });
    }

    private void listenForProgress() {
        progressRepository.listen(new GrammarProgressRepository.Callback() {
            @Override
            public void onProgress(Map<String, Integer> values) {
                progressByLesson.clear();
                progressByLesson.putAll(values);
                if (lessonsLoaded) {
                    renderMeta();
                    renderLessons(lessons);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(
                        GrammarTopicsActivity.this,
                        "Progress could not be synced. Please try again.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void renderMeta() {
        String curriculum = curriculumMeta.curriculum == null
                || curriculumMeta.curriculum.trim().isEmpty()
                ? "Grammar" : curriculumMeta.curriculum;
        ((TextView) findViewById(R.id.grammarHeroTitle)).setText(curriculum);
        ((TextView) findViewById(R.id.grammarHeroSubtitle))
                .setText("Live lessons from your English Foundation curriculum.");

        int total = lessons.size();
        int completed = 0;
        int progressTotal = 0;
        for (GrammarLesson lesson : lessons) {
            int progress = lessonProgress(lesson);
            progressTotal += progress;
            if (progress >= 100) completed++;
        }
        int overallProgress = total == 0 ? 0 : Math.round(progressTotal / (float) total);

        ((TextView) findViewById(R.id.grammarProgressValue))
                .setText(overallProgress + "%");
        ((TextView) findViewById(R.id.grammarLessonCount))
                .setText("Your Grammar Progress");
        ((TextView) findViewById(R.id.grammarCompletedCount))
                .setText(completed + " / " + total + " Lessons Completed");

        ProgressBar progress = findViewById(R.id.grammarCurriculumProgress);
        progress.setMax(100);
        ObjectAnimator animation = ObjectAnimator.ofInt(
                progress,
                "progress",
                progress.getProgress(),
                overallProgress
        );
        animation.setDuration(650);
        animation.start();
        ((TextView) findViewById(R.id.grammarCurriculumTarget))
                .setText(total + " live lessons \u2022 " + overallProgress + "% overall completed");
    }

    private void renderLessons(List<GrammarLesson> lessons) {
        topicContainer.removeAllViews();
        empty.setVisibility(lessons.isEmpty() ? View.VISIBLE : View.GONE);
        if (lessons.isEmpty()) {
            empty.setText("No grammar lessons are available yet.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int index = 0; index < lessons.size(); index++) {
            GrammarLesson lesson = lessons.get(index);
            View row = inflater.inflate(R.layout.item_grammar_topic, topicContainer, false);
            TextView icon = row.findViewById(R.id.topicIcon);
            TextView title = row.findViewById(R.id.topicTitle);
            TextView meta = row.findViewById(R.id.topicMeta);
            LessonProgressView progressView = row.findViewById(R.id.topicProgress);

            icon.setText(shortLabel(lesson));
            icon.setBackgroundResource(iconBackground(index));
            title.setText((lesson.order > 0 ? lesson.order + ". " : "") + lesson.displayName());
            String category = safe(lesson.category);
            String details = safe(lesson.level) + " • " + safe(lesson.difficulty)
                    + " • " + lesson.estimatedTime + " min";
            meta.setText(category.isEmpty() ? details : category + " • " + details);
            int progress = lessonProgress(lesson);
            int[] colors = progressColors(index);
            progressView.setAccentColors(colors[0], colors[1]);
            progressView.setProgress(0, false);
            progressView.post(() -> progressView.setProgress(progress, true));
            row.setOnClickListener(view -> startActivity(
                    new Intent(this, GrammarLessonActivity.class)
                            .putExtra("lesson_id", lesson.id)));
            topicContainer.addView(row);
        }
    }

    private String shortLabel(GrammarLesson lesson) {
        String value = !safe(lesson.subCategory).isEmpty()
                ? lesson.subCategory : lesson.displayName();
        String[] words = value.trim().split("\\s+");
        return words.length == 1
                ? words[0].substring(0, Math.min(2, words[0].length())).toUpperCase()
                : (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
    }

    private int iconBackground(int index) {
        int[] values = {
                R.drawable.bg_grammar_icon_green,
                R.drawable.bg_grammar_icon_orange,
                R.drawable.bg_grammar_icon_blue,
                R.drawable.bg_grammar_icon_pink,
                R.drawable.bg_grammar_icon_cyan,
                R.drawable.bg_grammar_icon_light_green
        };
        return values[index % values.length];
    }

    private int[] progressColors(int index) {
        int[][] colors = {
                {Color.parseColor("#16A34A"), Color.parseColor("#34D399")},
                {Color.parseColor("#F97316"), Color.parseColor("#FACC15")},
                {Color.parseColor("#2563EB"), Color.parseColor("#22D3EE")},
                {Color.parseColor("#DB2777"), Color.parseColor("#FB7185")},
                {Color.parseColor("#7C3AED"), Color.parseColor("#A78BFA")},
                {Color.parseColor("#0891B2"), Color.parseColor("#2DD4BF")}
        };
        return colors[index % colors.length];
    }

    private int lessonProgress(GrammarLesson lesson) {
        Integer value = progressByLesson.get(safe(lesson.id));
        return value == null ? 0 : Math.max(0, Math.min(100, value));
    }

    private void showLoading(boolean visible) {
        loading.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) empty.setVisibility(View.GONE);
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }

    @Override
    protected void onDestroy() {
        repository.stop();
        progressRepository.stop();
        super.onDestroy();
    }
}
