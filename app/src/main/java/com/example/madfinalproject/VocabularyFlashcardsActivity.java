package com.example.madfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madfinalproject.foundation.VocabularyWord;
import com.example.madfinalproject.foundation.VocabularyChallengeRepository;

public class VocabularyFlashcardsActivity extends VocabularyBaseActivity {
    private final VocabularyChallengeRepository challengeRepository =
            new VocabularyChallengeRepository();
    private int currentIndex;
    private boolean flipped;
    private VocabularyWord currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_flashcards);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.flashcard).setOnClickListener(view -> flip());
        findViewById(R.id.btnSpeak).setOnClickListener(
                view -> { if (currentWord != null) speak(currentWord.word); });
        findViewById(R.id.btnDifficult).setOnClickListener(
                view -> rate(25, "difficult"));
        findViewById(R.id.btnLearning).setOnClickListener(
                view -> rate(60, "learning"));
        findViewById(R.id.btnKnown).setOnClickListener(
                view -> rate(100, "known"));
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {
        if (words.isEmpty()) return;
        currentIndex = Math.min(currentIndex, words.size() - 1);
        render();
    }

    private void render() {
        currentWord = words.get(currentIndex);
        flipped = false;
        VocabularyWord.Flashcard flashcard = currentWord.safeFlashcard();
        String front = empty(flashcard.front) ? currentWord.word : flashcard.front;
        String back = empty(flashcard.back) ? currentWord.displayMeaning() : flashcard.back;
        ((TextView) findViewById(R.id.cardWord)).setText(front);
        ((TextView) findViewById(R.id.cardBack)).setText(back);
        findViewById(R.id.cardBack).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.cardHint)).setText("Tap the card to see meaning");
        ((TextView) findViewById(R.id.cardCounter))
                .setText((currentIndex + 1) + " / " + words.size());
        ProgressBar progress = findViewById(R.id.cardProgress);
        progress.setProgress(Math.round((currentIndex + 1) * 100f / words.size()), true);
    }

    private void flip() {
        flipped = !flipped;
        findViewById(R.id.cardBack).setVisibility(flipped ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.cardHint))
                .setText(flipped ? "Rate how well you know this word" : "Tap the card to see meaning");
    }

    private void rate(int progress, String status) {
        if (currentWord == null) return;
        vocabularyProgress.save(currentWord.id, progress, status);
        if (progress >= 100) challengeRepository.recordLearnedWord(currentWord.id);
        if (currentIndex >= words.size() - 1) {
            Toast.makeText(this, "Flashcard session completed.", Toast.LENGTH_SHORT).show();
            open(VocabularyQuizActivity.class);
            finish();
            return;
        }
        currentIndex++;
        render();
    }

    @Override
    protected void onDestroy() {
        challengeRepository.stop();
        super.onDestroy();
    }
}
