package com.example.madfinalproject.foundation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VocabularyChallengeRepository {
    public interface Callback {
        void onChallenge(DailyChallenge challenge);
        void onError(String message);
    }

    public static class DailyChallenge {
        public List<String> learnedWordIds = new ArrayList<>();
        public boolean quizCompleted;
        public boolean speakingCompleted;
        public int bestScore;

        public int completedTasks() {
            int completed = learnedWordIds != null && learnedWordIds.size() >= 10 ? 1 : 0;
            if (quizCompleted) completed++;
            if (speakingCompleted) completed++;
            if (bestScore >= 90) completed++;
            return completed;
        }
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    public void listenToday(Callback callback) {
        stop();
        DocumentReference reference = todayReference();
        if (reference == null) {
            callback.onChallenge(new DailyChallenge());
            return;
        }
        listener = reference.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                callback.onError(error.getMessage());
                return;
            }
            DailyChallenge challenge = snapshot == null
                    ? null : snapshot.toObject(DailyChallenge.class);
            callback.onChallenge(challenge == null ? new DailyChallenge() : challenge);
        });
    }

    public void recordLearnedWord(String wordId) {
        DocumentReference reference = todayReference();
        if (reference == null || wordId == null || wordId.trim().isEmpty()) return;
        Map<String, Object> values = commonValues();
        values.put("learnedWordIds", FieldValue.arrayUnion(wordId));
        reference.set(values, SetOptions.merge());
    }

    public void recordQuiz(boolean completed) {
        DocumentReference reference = todayReference();
        if (reference == null) return;
        Map<String, Object> values = commonValues();
        values.put("quizCompleted", completed);
        reference.set(values, SetOptions.merge());
    }

    public void recordSpeaking(int score) {
        DocumentReference reference = todayReference();
        if (reference == null) return;
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reference);
            Long previous = snapshot.getLong("bestScore");
            Map<String, Object> values = commonValues();
            values.put("speakingCompleted", true);
            values.put("bestScore", Math.max(previous == null ? 0 : previous.intValue(), score));
            transaction.set(reference, values, SetOptions.merge());
            return null;
        });
    }

    private DocumentReference todayReference() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) return null;
        return firestore.collection("userVocabularyChallenges")
                .document(uid)
                .collection("days")
                .document(todayKey());
    }

    private Map<String, Object> commonValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("dateKey", todayKey());
        values.put("updatedAt", FieldValue.serverTimestamp());
        return values;
    }

    private String todayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    public void stop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
