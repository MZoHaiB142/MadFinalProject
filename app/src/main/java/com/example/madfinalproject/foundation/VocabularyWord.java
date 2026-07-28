package com.example.madfinalproject.foundation;

import java.util.ArrayList;
import java.util.List;

public class VocabularyWord {
    public String id = "";
    public String word = "";
    public String normalizedWord = "";
    public String level = "";
    public String difficulty = "";
    public String module = "";
    public String category = "";
    public String subCategory = "";
    public String partOfSpeech = "";
    public String ipa = "";
    public String pronunciation = "";
    public String meaning = "";
    public String urduMeaning = "";
    public String simpleMeaning = "";
    public List<String> synonyms = new ArrayList<>();
    public List<String> antonyms = new ArrayList<>();
    public List<String> collocations = new ArrayList<>();
    public WordFamily wordFamily = new WordFamily();
    public List<ExampleSentence> exampleSentences = new ArrayList<>();
    public List<String> commonMistakes = new ArrayList<>();
    public String memoryTip = "";
    public boolean ielts;
    public boolean pte;
    public boolean toefl;
    public boolean businessEnglish;
    public boolean academicEnglish;
    public String frequency = "";
    public List<String> topicTags = new ArrayList<>();
    public String audioFile = "";
    public String imageUrl = "";
    public Flashcard flashcard = new Flashcard();
    public Quiz quiz = new Quiz();
    public SpeakingPractice speakingPractice = new SpeakingPractice();
    public List<String> relatedWords = new ArrayList<>();
    public boolean bookmark;
    public int estimatedLearningTime;
    public int xp;

    public VocabularyWord() {}

    public String displayMeaning() {
        return notEmpty(simpleMeaning) ? simpleMeaning : meaning;
    }

    public Flashcard safeFlashcard() {
        return flashcard == null ? new Flashcard() : flashcard;
    }

    public Quiz safeQuiz() {
        return quiz == null ? new Quiz() : quiz;
    }

    public WordFamily safeWordFamily() {
        return wordFamily == null ? new WordFamily() : wordFamily;
    }

    public SpeakingPractice safeSpeakingPractice() {
        return speakingPractice == null ? new SpeakingPractice() : speakingPractice;
    }

    public <T> List<T> safe(List<T> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class WordFamily {
        public String noun = "";
        public String verb = "";
        public String adverb = "";
        public String adjective = "";
        public WordFamily() {}
    }

    public static class ExampleSentence {
        public String english = "";
        public String urdu = "";
        public String explanation = "";
        public ExampleSentence() {}
    }

    public static class Flashcard {
        public String front = "";
        public String back = "";
        public Flashcard() {}
    }

    public static class Quiz {
        public String question = "";
        public List<String> options = new ArrayList<>();
        public int correctAnswer;
        public String explanation = "";
        public Quiz() {}
    }

    public static class SpeakingPractice {
        public String sentence = "";
        public List<String> focusWords = new ArrayList<>();
        public SpeakingPractice() {}
    }
}
