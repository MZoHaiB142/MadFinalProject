package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.foundation.VocabularyProgressRepository;
import com.example.madfinalproject.foundation.VocabularyRepository;
import com.example.madfinalproject.foundation.VocabularyWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class VocabularyBaseActivity extends AppCompatActivity {
    protected final VocabularyRepository vocabularyRepository = new VocabularyRepository();
    protected final VocabularyProgressRepository vocabularyProgress =
            new VocabularyProgressRepository();
    protected final List<VocabularyWord> words = new ArrayList<>();
    private TextToSpeech textToSpeech;
    private boolean speechReady;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new TextToSpeech(this, status -> {
            speechReady = status == TextToSpeech.SUCCESS;
            if (speechReady) textToSpeech.setLanguage(Locale.US);
        });
    }

    protected void listenForVocabulary() {
        setLoading(true);
        vocabularyRepository.listen(new VocabularyRepository.Callback() {
            @Override
            public void onData(List<VocabularyWord> values) {
                words.clear();
                words.addAll(values);
                setLoading(false);
                setError(words.isEmpty() ? "No vocabulary words are available." : "");
                onVocabularyUpdated();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                setError("Unable to load vocabulary. " + message);
            }
        });
    }

    protected abstract void onVocabularyUpdated();

    protected void speak(String text) {
        if (!speechReady || empty(text)) return;
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocabulary_word");
    }

    protected void open(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    protected void open(Class<?> activity, String wordId) {
        startActivity(new Intent(this, activity).putExtra("word_id", wordId));
    }

    protected int findWord(String wordId) {
        for (int index = 0; index < words.size(); index++) {
            if (safe(words.get(index).id).equals(safe(wordId))) return index;
        }
        return 0;
    }

    protected String safe(String value) {
        return value == null ? "" : value.trim();
    }

    protected boolean empty(String value) {
        return safe(value).isEmpty();
    }

    protected String join(List<String> values) {
        if (values == null || values.isEmpty()) return "Not available";
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (empty(value)) continue;
            if (result.length() > 0) result.append(", ");
            result.append(safe(value));
        }
        return result.length() == 0 ? "Not available" : result.toString();
    }

    private void setLoading(boolean visible) {
        View loading = findViewById(R.id.loading);
        if (loading != null) loading.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setError(String message) {
        TextView error = findViewById(R.id.errorText);
        if (error == null) return;
        error.setText(message);
        error.setVisibility(empty(message) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        vocabularyRepository.stop();
        vocabularyProgress.stop();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
