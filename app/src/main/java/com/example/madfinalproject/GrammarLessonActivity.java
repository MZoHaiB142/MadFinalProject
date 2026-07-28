package com.example.madfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.foundation.GrammarCurriculumMeta;
import com.example.madfinalproject.foundation.GrammarLesson;
import com.example.madfinalproject.foundation.GrammarProgressRepository;
import com.example.madfinalproject.foundation.GrammarRepository;

import java.util.ArrayList;
import java.util.List;

public class GrammarLessonActivity extends AppCompatActivity {
    private final GrammarRepository repository = new GrammarRepository();
    private final GrammarProgressRepository progressRepository = new GrammarProgressRepository();
    private final List<GrammarLesson> lessons = new ArrayList<>();
    private String requestedLessonId = "";
    private int currentIndex;
    private int selectedTab;
    private GrammarLesson currentLesson;
    private TextView tabLesson, tabExamples, tabNotes;
    private TextView contentHeading, definition, examplesHeading, examples, tipText;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_lesson);
        requestedLessonId = getIntent().getStringExtra("lesson_id");
        if (requestedLessonId == null) requestedLessonId = "";
        bindViews();
        listenForLesson();
    }

    private void bindViews() {
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnSave).setOnClickListener(view ->
                Toast.makeText(this, "Lesson saved.", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnPlay).setOnClickListener(view -> {
            saveCurrentProgress(40);
            Toast.makeText(this, "Lesson video started.", Toast.LENGTH_SHORT).show();
        });

        loading = findViewById(R.id.lessonLoading);
        tabLesson = findViewById(R.id.tabLesson);
        tabExamples = findViewById(R.id.tabExamples);
        tabNotes = findViewById(R.id.tabNotes);
        contentHeading = findViewById(R.id.contentHeading);
        definition = findViewById(R.id.lessonDefinition);
        examplesHeading = findViewById(R.id.examplesHeading);
        examples = findViewById(R.id.lessonExamples);
        tipText = findViewById(R.id.tipText);

        tabLesson.setOnClickListener(view -> selectTab(0));
        tabExamples.setOnClickListener(view -> selectTab(1));
        tabNotes.setOnClickListener(view -> selectTab(2));
        findViewById(R.id.btnPrevious).setOnClickListener(view -> previous());
        findViewById(R.id.btnNext).setOnClickListener(view -> next());
    }

    private void listenForLesson() {
        loading.setVisibility(View.VISIBLE);
        findViewById(R.id.contentScroll).setVisibility(View.INVISIBLE);
        repository.listen(new GrammarRepository.Callback() {
            @Override
            public void onData(GrammarCurriculumMeta meta, List<GrammarLesson> values) {
                lessons.clear();
                lessons.addAll(values);
                loading.setVisibility(View.GONE);
                if (lessons.isEmpty()) {
                    showError("No grammar lessons are available.");
                    return;
                }
                currentIndex = findLessonIndex(requestedLessonId);
                findViewById(R.id.contentScroll).setVisibility(View.VISIBLE);
                renderLesson();
            }

            @Override
            public void onError(String message) {
                loading.setVisibility(View.GONE);
                showError("Unable to load this lesson. " + message);
            }
        });
    }

    private int findLessonIndex(String lessonId) {
        for (int index = 0; index < lessons.size(); index++) {
            if (safe(lessons.get(index).id).equals(lessonId)) return index;
        }
        return 0;
    }

    private void renderLesson() {
        currentLesson = lessons.get(currentIndex);
        requestedLessonId = safe(currentLesson.id);
        setText(R.id.headerTitle, currentLesson.displayName());
        setText(R.id.videoTitle, currentLesson.displayName());
        setText(R.id.videoDuration, Math.max(1, currentLesson.estimatedTime) + ":00");
        ((Button) findViewById(R.id.btnPrevious)).setText(
                currentIndex == 0 ? "Back" : "Previous");
        ((Button) findViewById(R.id.btnNext)).setText(
                currentIndex == lessons.size() - 1 ? "Finish" : "Next");
        selectTab(selectedTab);
    }

    private void selectTab(int tab) {
        if (currentLesson == null) return;
        selectedTab = tab;
        styleTab(tabLesson, tab == 0);
        styleTab(tabExamples, tab == 1);
        styleTab(tabNotes, tab == 2);

        if (tab == 0) {
            contentHeading.setText(currentLesson.displayName());
            definition.setVisibility(View.VISIBLE);
            definition.setText(formatLessonContent(currentLesson));
            examplesHeading.setVisibility(View.VISIBLE);
            examplesHeading.setText("Examples");
            examples.setVisibility(View.VISIBLE);
            examples.setText(formatExamples(currentLesson.safe(currentLesson.examples)));
        } else if (tab == 1) {
            contentHeading.setText("Examples");
            definition.setVisibility(View.GONE);
            examplesHeading.setVisibility(View.VISIBLE);
            examplesHeading.setText(currentLesson.subCategory);
            examples.setVisibility(View.VISIBLE);
            examples.setText(formatExamples(currentLesson.safe(currentLesson.examples)));
        } else {
            contentHeading.setText("Notes & Review");
            definition.setVisibility(View.VISIBLE);
            definition.setText(formatNotes(currentLesson));
            examplesHeading.setVisibility(View.VISIBLE);
            examplesHeading.setText("Practice & Assessments");
            examples.setVisibility(View.VISIBLE);
            examples.setText(formatAssessments(currentLesson));
        }

        int[] tabProgress = {25, 60, 80};
        saveCurrentProgress(tabProgress[Math.max(0, Math.min(tab, tabProgress.length - 1))]);

        List<String> tips = currentLesson.safe(currentLesson.safeLesson().tips);
        tipText.setText(tips.isEmpty()
                ? safe(currentLesson.summary)
                : joinBullets(tips));
    }

    private String formatLessonContent(GrammarLesson lesson) {
        GrammarLesson.LessonContent content = lesson.safeLesson();
        StringBuilder text = new StringBuilder();
        appendParagraph(text, lesson.description);
        appendParagraph(text, content.introduction);
        appendSection(text, "Learning Objectives", lesson.safe(lesson.learningObjectives));
        appendSection(text, "Rules", lesson.safe(content.rules));
        if (!safe(content.structure).isEmpty()) {
            appendHeading(text, "Structure");
            appendParagraph(text, content.structure);
        }
        appendSection(text, "Usage", lesson.safe(content.usage));
        return text.toString().trim();
    }

    private String formatExamples(List<GrammarLesson.Example> values) {
        if (values.isEmpty()) return "No examples are available.";
        StringBuilder text = new StringBuilder();
        for (GrammarLesson.Example value : values) {
            appendParagraph(text, "• " + safe(value.english));
            if (!safe(value.explanation).isEmpty()) {
                text.append("   ").append(safe(value.explanation)).append("\n\n");
            }
        }
        return text.toString().trim();
    }

    private String formatNotes(GrammarLesson lesson) {
        GrammarLesson.LessonContent content = lesson.safeLesson();
        StringBuilder text = new StringBuilder();
        appendSection(text, "Common Mistakes", lesson.safe(content.commonMistakes));
        appendSection(text, "Tips", lesson.safe(content.tips));
        if (!safe(lesson.summary).isEmpty()) {
            appendHeading(text, "Summary");
            appendParagraph(text, lesson.summary);
        }
        appendSection(text, "Review Questions", lesson.safe(lesson.reviewQuestions));
        text.append("\nEstimated time: ").append(lesson.estimatedTime).append(" minutes");
        text.append("\nReward: ").append(lesson.xp).append(" XP");
        return text.toString().trim();
    }

    private String formatAssessments(GrammarLesson lesson) {
        StringBuilder text = new StringBuilder();
        List<GrammarLesson.Mcq> mcqs = lesson.safe(lesson.mcqs);
        if (!mcqs.isEmpty()) {
            appendHeading(text, "Multiple Choice Questions");
            for (GrammarLesson.Mcq value : mcqs) {
                appendParagraph(text, "• " + safe(value.question));
            }
        }
        List<GrammarLesson.FillBlank> blanks = lesson.safe(lesson.fillInTheBlanks);
        if (!blanks.isEmpty()) {
            appendHeading(text, "Fill in the Blanks");
            for (GrammarLesson.FillBlank value : blanks) {
                appendParagraph(text, "• " + safe(value.question));
            }
        }
        List<GrammarLesson.Correction> corrections = lesson.safe(lesson.sentenceCorrection);
        if (!corrections.isEmpty()) {
            appendHeading(text, "Sentence Correction");
            for (GrammarLesson.Correction value : corrections) {
                appendParagraph(text, "• " + safe(value.incorrect));
            }
        }
        List<GrammarLesson.Practice> practice = lesson.safe(lesson.practice);
        if (!practice.isEmpty()) {
            appendHeading(text, "Practice");
            for (GrammarLesson.Practice value : practice) {
                appendParagraph(text, "• " + safe(value.question));
            }
        }
        return text.length() == 0 ? "No assessment content is available." : text.toString().trim();
    }

    private void appendSection(StringBuilder text, String title, List<String> values) {
        if (values.isEmpty()) return;
        appendHeading(text, title);
        text.append(joinBullets(values)).append("\n\n");
    }

    private void appendHeading(StringBuilder text, String title) {
        if (text.length() > 0) text.append("\n");
        text.append(title).append("\n");
    }

    private void appendParagraph(StringBuilder text, String value) {
        if (!safe(value).isEmpty()) text.append(safe(value)).append("\n\n");
    }

    private String joinBullets(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) {
            if (!safe(value).isEmpty()) {
                if (text.length() > 0) text.append("\n");
                text.append("• ").append(safe(value));
            }
        }
        return text.toString();
    }

    private void styleTab(TextView view, boolean selected) {
        view.setTextColor(android.graphics.Color.parseColor(selected ? "#5B35DD" : "#52617C"));
        view.setBackgroundResource(selected
                ? R.drawable.bg_grammar_selected_tab : android.R.color.transparent);
    }

    private void previous() {
        if (currentIndex == 0) finish();
        else {
            currentIndex--;
            selectedTab = 0;
            renderLesson();
            findViewById(R.id.contentScroll).scrollTo(0, 0);
        }
    }

    private void next() {
        saveCurrentProgress(100);
        if (currentIndex >= lessons.size() - 1) {
            Toast.makeText(this, "Grammar lessons completed.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            currentIndex++;
            selectedTab = 0;
            renderLesson();
            findViewById(R.id.contentScroll).scrollTo(0, 0);
        }
    }

    private void saveCurrentProgress(int progress) {
        if (currentLesson == null) return;
        progressRepository.saveProgress(safe(currentLesson.id), progress);
    }

    private void showError(String message) {
        findViewById(R.id.contentScroll).setVisibility(View.INVISIBLE);
        TextView error = findViewById(R.id.lessonError);
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }

    @Override
    protected void onDestroy() {
        repository.stop();
        progressRepository.stop();
        super.onDestroy();
    }
}
