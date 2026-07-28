package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyUserProgress;
import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VocabularyBookmarksActivity extends VocabularyBaseActivity {
    private final Map<String, VocabularyUserProgress> progress = new HashMap<>();
    private LinearLayout container;
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_bookmarks);
        container = findViewById(R.id.bookmarkContainer);
        search = findViewById(R.id.searchBookmarks);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { render(); }
            public void afterTextChanged(Editable s) {}
        });
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
        if (container == null) return;
        container.removeAllViews();
        String query = search == null ? ""
                : safe(search.getText().toString()).toLowerCase(Locale.US);
        int count = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyWord word : words) {
            VocabularyUserProgress value = progress.get(word.id);
            if (value == null || !value.bookmarked) continue;
            if (!query.isEmpty() && !safe(word.word).toLowerCase(Locale.US).contains(query)) {
                continue;
            }
            View row = inflater.inflate(R.layout.item_vocabulary_word, container, false);
            ((TextView) row.findViewById(R.id.wordTitle)).setText(word.word);
            ((TextView) row.findViewById(R.id.wordMeaning)).setText(word.displayMeaning());
            TextView star = row.findViewById(R.id.wordProgress);
            star.setText("★");
            star.setTextSize(20);
            star.setOnClickListener(view -> vocabularyProgress.setBookmarked(word.id, false));
            row.findViewById(R.id.btnWordSpeak).setOnClickListener(view -> speak(word.word));
            row.setOnClickListener(view -> startActivity(
                    new Intent(this, VocabularyDetailActivity.class)
                            .putExtra("word_id", word.id)));
            container.addView(row);
            count++;
        }
        findViewById(R.id.emptyBookmarks).setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }
}
