package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.foundation.VocabularyRepository;
import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.List;

public class EnglishFoundationActivity extends AppCompatActivity {
    private final VocabularyRepository vocabularyRepository = new VocabularyRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_foundation);
        findViewById(R.id.cardGrammar).setOnClickListener(view ->
                startActivity(new Intent(this, GrammarTopicsActivity.class)));
        findViewById(R.id.cardVocabulary).setOnClickListener(view ->
                startActivity(new Intent(this, VocabularyHomeActivity.class)));
        findViewById(R.id.cardReading).setOnClickListener(view -> open("grammar"));
        findViewById(R.id.cardWriting).setOnClickListener(view -> open("grammar"));
        findViewById(R.id.cardSpeaking).setOnClickListener(view -> open("grammar"));
        findViewById(R.id.cardListening).setOnClickListener(view -> open("grammar"));
        findViewById(R.id.cardContinue).setOnClickListener(view -> open("parts"));
        findViewById(R.id.btnQuiz).setOnClickListener(view -> open("quiz"));
        findViewById(R.id.btnProgress).setOnClickListener(view -> open("progress"));
        findViewById(R.id.btnProgressFooter).setOnClickListener(view -> open("progress"));
        listenForVocabularyCount();
    }

    private void listenForVocabularyCount() {
        vocabularyRepository.listen(new VocabularyRepository.Callback() {
            @Override
            public void onData(List<VocabularyWord> words) {
                ((TextView) findViewById(R.id.vocabularyWordCount))
                        .setText(words.size() + " Words");
            }

            @Override
            public void onError(String message) {
                ((TextView) findViewById(R.id.vocabularyWordCount))
                        .setText("Live Words");
            }
        });
    }

    private void open(String screen) {
        startActivity(new Intent(this, FoundationFlowActivity.class)
                .putExtra("screen", screen));
    }

    @Override
    protected void onDestroy() {
        vocabularyRepository.stop();
        super.onDestroy();
    }
}
