package com.example.madfinalproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madfinalproject.foundation.VocabularyWord;
import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyUserProgress;

import java.util.HashMap;
import java.util.Map;

public class VocabularyDetailActivity extends VocabularyBaseActivity {
    private String requestedWordId = "";
    private int currentIndex;
    private VocabularyWord currentWord;
    private boolean bookmarked;
    private final Map<String, VocabularyUserProgress> progressByWord = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_detail);
        requestedWordId = safe(getIntent().getStringExtra("word_id"));
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnSpeak).setOnClickListener(
                view -> { if (currentWord != null) speak(currentWord.word); });
        findViewById(R.id.btnBookmark).setOnClickListener(view -> toggleBookmark());
        findViewById(R.id.btnPractice).setOnClickListener(
                view -> { if (currentWord != null) open(VocabularyQuizActivity.class, currentWord.id); });
        findViewById(R.id.btnNextWord).setOnClickListener(view -> nextWord());
        vocabularyProgress.listenDetails(new VocabularyProgressRepository.DetailCallback() {
            @Override
            public void onProgress(Map<String, VocabularyUserProgress> values) {
                progressByWord.clear();
                progressByWord.putAll(values);
                syncBookmark();
            }

            @Override
            public void onError(String message) {}
        });
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (words.isEmpty()) return;
        currentIndex = findWord(requestedWordId);
        render();
    }

    private void render() {
        currentWord = words.get(currentIndex);
        requestedWordId = currentWord.id;
        ((TextView) findViewById(R.id.headerTitle)).setText(currentWord.word);
        ((TextView) findViewById(R.id.detailWord)).setText(currentWord.word);
        ((TextView) findViewById(R.id.detailPronunciation)).setText(
                currentWord.pronunciation + (empty(currentWord.ipa) ? "" : "   " + currentWord.ipa));
        ((TextView) findViewById(R.id.detailBadges)).setText(
                currentWord.level + "  •  " + currentWord.difficulty
                        + "  •  " + currentWord.partOfSpeech);
        ((TextView) findViewById(R.id.detailMeaning)).setText(
                currentWord.meaning + "\n\nSimple meaning\n" + currentWord.displayMeaning());
        ((TextView) findViewById(R.id.detailRelations)).setText(
                "Synonyms\n" + join(currentWord.synonyms)
                        + "\n\nAntonyms\n" + join(currentWord.antonyms)
                        + "\n\nRelated words\n" + join(currentWord.relatedWords));
        ((TextView) findViewById(R.id.detailExamples))
                .setText(formatExamples(currentWord));
        ((TextView) findViewById(R.id.detailUsage))
                .setText(formatUsage(currentWord));
        syncBookmark();
        vocabularyProgress.save(currentWord.id, 25, "viewed");
    }

    private void syncBookmark() {
        if (currentWord == null) return;
        VocabularyUserProgress value = progressByWord.get(currentWord.id);
        bookmarked = value != null && value.bookmarked;
        ((TextView) findViewById(R.id.btnBookmark)).setText(bookmarked ? "★" : "☆");
    }

    private String formatExamples(VocabularyWord word) {
        StringBuilder value = new StringBuilder();
        for (VocabularyWord.ExampleSentence example : word.safe(word.exampleSentences)) {
            if (value.length() > 0) value.append("\n\n");
            value.append("• ").append(safe(example.english));
            if (!empty(example.explanation)) {
                value.append("\n  ").append(safe(example.explanation));
            }
        }
        return value.length() == 0 ? "No examples available." : value.toString();
    }

    private String formatUsage(VocabularyWord word) {
        VocabularyWord.WordFamily family = word.safeWordFamily();
        String familyText = "Noun: " + safe(family.noun)
                + "  •  Verb: " + safe(family.verb)
                + "\nAdjective: " + safe(family.adjective)
                + "  •  Adverb: " + safe(family.adverb);
        return "Collocations\n" + join(word.collocations)
                + "\n\nWord family\n" + familyText
                + "\n\nMemory tip\n" + safe(word.memoryTip)
                + "\n\nCommon mistakes\n• " + join(word.commonMistakes);
    }

    private void toggleBookmark() {
        if (currentWord == null) return;
        bookmarked = !bookmarked;
        ((TextView) findViewById(R.id.btnBookmark)).setText(bookmarked ? "★" : "☆");
        vocabularyProgress.setBookmarked(currentWord.id, bookmarked);
        Toast.makeText(this, bookmarked ? "Word bookmarked." : "Bookmark removed.",
                Toast.LENGTH_SHORT).show();
    }

    private void nextWord() {
        if (words.isEmpty()) return;
        currentIndex = (currentIndex + 1) % words.size();
        bookmarked = false;
        ((TextView) findViewById(R.id.btnBookmark)).setText("☆");
        render();
    }
}
