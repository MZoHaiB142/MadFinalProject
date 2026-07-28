package com.example.madfinalproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VocabularyWordsActivity extends VocabularyBaseActivity {
    private final Map<String, Integer> progressByWord = new HashMap<>();
    private LinearLayout container;
    private EditText search;
    private String selectedDifficulty = "Beginner";
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_words);
        selectedCategory = safe(getIntent().getStringExtra("category"));
        container = findViewById(R.id.wordContainer);
        search = findViewById(R.id.searchWords);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.tabBeginner).setOnClickListener(view -> selectDifficulty("Beginner"));
        findViewById(R.id.tabIntermediate).setOnClickListener(view -> selectDifficulty("Intermediate"));
        findViewById(R.id.tabAdvanced).setOnClickListener(view -> selectDifficulty("Advanced"));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { render(); }
            public void afterTextChanged(Editable s) {}
        });
        if (!empty(selectedCategory)) {
            ((TextView) findViewById(R.id.wordsTitle)).setText(selectedCategory);
        }
        vocabularyProgress.listen(new VocabularyProgressRepository.Callback() {
            public void onProgress(Map<String, Integer> values) {
                progressByWord.clear();
                progressByWord.putAll(values);
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

    private void selectDifficulty(String value) {
        selectedDifficulty = value;
        styleTab(R.id.tabBeginner, "Beginner".equals(value));
        styleTab(R.id.tabIntermediate, "Intermediate".equals(value));
        styleTab(R.id.tabAdvanced, "Advanced".equals(value));
        render();
    }

    private void styleTab(int id, boolean selected) {
        TextView view = findViewById(id);
        view.setTextColor(Color.parseColor(selected ? "#5B35DD" : "#748096"));
        view.setTypeface(null, selected
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void render() {
        if (container == null) return;
        container.removeAllViews();
        String query = search == null ? "" : safe(search.getText().toString()).toLowerCase(Locale.US);
        int count = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyWord word : words) {
            if (!selectedDifficulty.equalsIgnoreCase(safe(word.difficulty))) continue;
            if (!empty(selectedCategory)
                    && !selectedCategory.equalsIgnoreCase(safe(word.category))) continue;
            if (!query.isEmpty()
                    && !safe(word.word).toLowerCase(Locale.US).contains(query)
                    && !safe(word.meaning).toLowerCase(Locale.US).contains(query)) continue;

            View row = inflater.inflate(R.layout.item_vocabulary_word, container, false);
            ((TextView) row.findViewById(R.id.wordTitle))
                    .setText(word.word + "  •  " + word.partOfSpeech);
            ((TextView) row.findViewById(R.id.wordMeaning)).setText(word.displayMeaning());
            int progress = progressByWord.getOrDefault(safe(word.id), 0);
            ((TextView) row.findViewById(R.id.wordProgress)).setText(progress + "%");
            row.findViewById(R.id.btnWordSpeak).setOnClickListener(view -> speak(word.word));
            row.setOnClickListener(view -> startActivity(
                    new Intent(this, VocabularyDetailActivity.class)
                            .putExtra("word_id", word.id)));
            container.addView(row);
            count++;
        }
        TextView emptyText = findViewById(R.id.emptyText);
        emptyText.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        if (count == 0 && !words.isEmpty()) {
            emptyText.setText("No " + selectedDifficulty.toLowerCase(Locale.US)
                    + " words are available for this filter.");
        }
    }
}
