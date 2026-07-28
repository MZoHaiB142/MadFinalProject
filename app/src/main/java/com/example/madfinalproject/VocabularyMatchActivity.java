package com.example.madfinalproject;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyMatchActivity extends VocabularyBaseActivity {
    private final List<Pair> pairs = new ArrayList<>();
    private Button selectedLeft;
    private int matched;
    private boolean gameStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_match);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnRestart).setOnClickListener(view -> startGame());
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (!gameStarted && !words.isEmpty()) startGame();
    }

    private void startGame() {
        if (words.isEmpty()) return;
        gameStarted = true;
        matched = 0;
        selectedLeft = null;
        pairs.clear();
        for (VocabularyWord word : words) {
            String match = !word.safe(word.synonyms).isEmpty()
                    ? word.synonyms.get(0) : word.displayMeaning();
            if (!empty(match)) pairs.add(new Pair(word.id, word.word, match));
            if (pairs.size() == 4) break;
        }
        renderGame();
    }

    private void renderGame() {
        LinearLayout left = findViewById(R.id.leftMatches);
        LinearLayout right = findViewById(R.id.rightMatches);
        left.removeAllViews();
        right.removeAllViews();
        List<Pair> shuffled = new ArrayList<>(pairs);
        Collections.shuffle(shuffled);
        for (Pair pair : pairs) {
            Button button = matchButton(pair.word);
            button.setTag(pair.id);
            button.setOnClickListener(view -> selectLeft(button));
            left.addView(button);
        }
        for (Pair pair : shuffled) {
            Button button = matchButton(pair.meaning);
            button.setTag(pair.id);
            button.setOnClickListener(view -> selectRight(button));
            right.addView(button);
        }
        updateProgress();
        ((TextView) findViewById(R.id.matchStatus))
                .setText("Select a word, then select its match.");
    }

    private Button matchButton(String text) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        params.setMargins(0, dp(6), 0, dp(6));
        button.setLayoutParams(params);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(11);
        button.setTextColor(Color.parseColor("#17294D"));
        button.setBackground(optionBackground("#FFFFFF", "#DDD8EF"));
        return button;
    }

    private void selectLeft(Button button) {
        if (!button.isEnabled()) return;
        if (selectedLeft != null) {
            selectedLeft.setBackground(optionBackground("#FFFFFF", "#DDD8EF"));
        }
        selectedLeft = button;
        button.setBackground(optionBackground("#EEE9FF", "#6D4CEB"));
        ((TextView) findViewById(R.id.matchStatus)).setText("Now select the matching meaning.");
    }

    private void selectRight(Button right) {
        if (!right.isEnabled()) return;
        if (selectedLeft == null) {
            ((TextView) findViewById(R.id.matchStatus)).setText("Select a word first.");
            return;
        }
        if (String.valueOf(selectedLeft.getTag()).equals(String.valueOf(right.getTag()))) {
            selectedLeft.setEnabled(false);
            right.setEnabled(false);
            selectedLeft.setBackground(optionBackground("#E8F8EE", "#34B765"));
            right.setBackground(optionBackground("#E8F8EE", "#34B765"));
            vocabularyProgress.save(String.valueOf(right.getTag()), 100, "matched");
            selectedLeft = null;
            matched++;
            updateProgress();
            ((TextView) findViewById(R.id.matchStatus)).setText(
                    matched == pairs.size()
                            ? "Excellent! All words matched."
                            : "Correct! Continue matching.");
        } else {
            selectedLeft.setBackground(optionBackground("#FFF0F1", "#EF5350"));
            right.setBackground(optionBackground("#FFF0F1", "#EF5350"));
            ((TextView) findViewById(R.id.matchStatus))
                    .setText("That pair does not match. Try again.");
            Button previousLeft = selectedLeft;
            selectedLeft = null;
            right.postDelayed(() -> {
                if (previousLeft.isEnabled()) {
                    previousLeft.setBackground(optionBackground("#FFFFFF", "#DDD8EF"));
                }
                if (right.isEnabled()) {
                    right.setBackground(optionBackground("#FFFFFF", "#DDD8EF"));
                }
            }, 500);
        }
    }

    private void updateProgress() {
        int progress = pairs.isEmpty() ? 0 : Math.round(matched * 100f / pairs.size());
        ((ProgressBar) findViewById(R.id.matchProgress)).setProgress(progress, true);
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

    private static final class Pair {
        final String id;
        final String word;
        final String meaning;
        Pair(String id, String word, String meaning) {
            this.id = id;
            this.word = word;
            this.meaning = meaning;
        }
    }
}
