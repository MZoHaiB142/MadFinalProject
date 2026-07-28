package com.example.madfinalproject.foundation;

import java.util.ArrayList;
import java.util.List;

public class GrammarLesson {
    public String id = "";
    public String module = "";
    public String category = "";
    public String subCategory = "";
    public int order;
    public String level = "";
    public String difficulty = "";
    public String title = "";
    public String description = "";
    public List<String> learningObjectives = new ArrayList<>();
    public LessonContent lesson = new LessonContent();
    public List<Example> examples = new ArrayList<>();
    public List<Mcq> mcqs = new ArrayList<>();
    public List<FillBlank> fillInTheBlanks = new ArrayList<>();
    public List<Correction> sentenceCorrection = new ArrayList<>();
    public List<Practice> practice = new ArrayList<>();
    public String summary = "";
    public List<String> reviewQuestions = new ArrayList<>();
    public int estimatedTime;
    public int xp;

    public GrammarLesson() {}

    public static class LessonContent {
        public String introduction = "";
        public List<String> rules = new ArrayList<>();
        public String structure = "";
        public List<String> usage = new ArrayList<>();
        public List<String> commonMistakes = new ArrayList<>();
        public List<String> tips = new ArrayList<>();
        public LessonContent() {}
    }

    public static class Example {
        public String english = "";
        public String urdu = "";
        public String explanation = "";
        public Example() {}
    }

    public static class Mcq {
        public String id = "";
        public String question = "";
        public List<String> options = new ArrayList<>();
        public int correctAnswer;
        public String explanation = "";
        public Mcq() {}
    }

    public static class FillBlank {
        public String id = "";
        public String question = "";
        public String answer = "";
        public String explanation = "";
        public FillBlank() {}
    }

    public static class Correction {
        public String id = "";
        public String incorrect = "";
        public String correct = "";
        public String explanation = "";
        public Correction() {}
    }

    public static class Practice {
        public String id = "";
        public String question = "";
        public String answer = "";
        public Practice() {}
    }

    public String displayName() {
        return !title.trim().isEmpty() ? title : !subCategory.trim().isEmpty() ? subCategory : "Grammar Lesson";
    }

    public LessonContent safeLesson() { return lesson == null ? new LessonContent() : lesson; }
    public <T> List<T> safe(List<T> values) { return values == null ? new ArrayList<>() : values; }
}
