package com.example.madfinalproject.foundation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public final class VocabularyProgressRepository {
    public interface Callback {
        void onProgress(Map<String, Integer> progressByWord);
        void onError(String message);
    }

    public interface DetailCallback {
        void onProgress(Map<String, VocabularyUserProgress> progressByWord);
        void onError(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;
    private ListenerRegistration detailListener;

    public void listen(Callback callback) {
        stop();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            callback.onProgress(new HashMap<>());
            return;
        }
        listener = words(uid).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                callback.onError(error.getMessage());
                return;
            }
            Map<String, Integer> progress = new HashMap<>();
            if (snapshot != null) {
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    Long value = document.getLong("progressPercent");
                    progress.put(document.getId(), clamp(value == null ? 0 : value.intValue()));
                }
            }
            callback.onProgress(progress);
        });
    }

    public void listenDetails(DetailCallback callback) {
        if (detailListener != null) detailListener.remove();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            callback.onProgress(new HashMap<>());
            return;
        }
        detailListener = words(uid).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                callback.onError(error.getMessage());
                return;
            }
            Map<String, VocabularyUserProgress> values = new HashMap<>();
            if (snapshot != null) {
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    VocabularyUserProgress value = document.toObject(VocabularyUserProgress.class);
                    if (value == null) value = new VocabularyUserProgress();
                    value.wordId = document.getId();
                    if (document.getTimestamp("updatedAt") != null) {
                        value.updatedAtMillis = document.getTimestamp("updatedAt")
                                .toDate().getTime();
                    }
                    value.progressPercent = clamp(value.progressPercent);
                    values.put(document.getId(), value);
                }
            }
            callback.onProgress(values);
        });
    }

    public void save(String wordId, int percentage, String status) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty() || empty(wordId)) return;
        DocumentReference reference = words(uid).document(wordId);
        int requested = clamp(percentage);
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reference);
            Long currentValue = snapshot.getLong("progressPercent");
            int progress = Math.max(currentValue == null ? 0 : currentValue.intValue(), requested);
            Map<String, Object> data = new HashMap<>();
            data.put("wordId", wordId);
            data.put("progressPercent", progress);
            data.put("learned", progress >= 100);
            data.put("status", status == null ? "" : status);
            data.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(reference, data, SetOptions.merge());
            return null;
        });
    }

    public void setBookmarked(String wordId, boolean bookmarked) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty() || empty(wordId)) return;
        Map<String, Object> data = new HashMap<>();
        data.put("wordId", wordId);
        data.put("bookmarked", bookmarked);
        data.put("updatedAt", FieldValue.serverTimestamp());
        words(uid).document(wordId).set(data, SetOptions.merge());
    }

    public void recordQuizResult(String wordId, boolean correct) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty() || empty(wordId)) return;
        DocumentReference reference = words(uid).document(wordId);
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reference);
            Long attempts = snapshot.getLong("quizAttempts");
            Long correctAnswers = snapshot.getLong("quizCorrect");
            Long currentProgress = snapshot.getLong("progressPercent");
            int progress = Math.max(currentProgress == null ? 0 : currentProgress.intValue(),
                    correct ? 100 : 60);
            Map<String, Object> data = new HashMap<>();
            data.put("wordId", wordId);
            data.put("progressPercent", progress);
            data.put("learned", progress >= 100);
            data.put("status", correct ? "quiz_correct" : "quiz_attempted");
            data.put("quizAttempts", (attempts == null ? 0 : attempts.intValue()) + 1);
            data.put("quizCorrect",
                    (correctAnswers == null ? 0 : correctAnswers.intValue()) + (correct ? 1 : 0));
            data.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(reference, data, SetOptions.merge());
            return null;
        });
    }

    public void recordSpeakingScore(String wordId, int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty() || empty(wordId)) return;
        DocumentReference reference = words(uid).document(wordId);
        int safeScore = clamp(score);
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reference);
            Long previousScore = snapshot.getLong("speakingScore");
            Long currentProgress = snapshot.getLong("progressPercent");
            int bestScore = Math.max(previousScore == null ? 0 : previousScore.intValue(), safeScore);
            int progress = Math.max(currentProgress == null ? 0 : currentProgress.intValue(),
                    safeScore >= 80 ? 100 : 60);
            Map<String, Object> data = new HashMap<>();
            data.put("wordId", wordId);
            data.put("progressPercent", progress);
            data.put("learned", progress >= 100);
            data.put("status", "speaking");
            data.put("speakingScore", bestScore);
            data.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(reference, data, SetOptions.merge());
            return null;
        });
    }

    private com.google.firebase.firestore.CollectionReference words(String uid) {
        return firestore.collection("userVocabularyProgress")
                .document(uid)
                .collection("words");
    }

    public void stop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
        if (detailListener != null) {
            detailListener.remove();
            detailListener = null;
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
