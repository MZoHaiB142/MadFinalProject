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

public final class GrammarProgressRepository {
    public interface Callback {
        void onProgress(Map<String, Integer> progressByLesson);
        void onError(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    public void listen(Callback callback) {
        stop();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty()) {
            callback.onProgress(new HashMap<>());
            return;
        }

        listener = firestore.collection("userFoundationProgress")
                .document(uid)
                .collection("grammar")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    Map<String, Integer> values = new HashMap<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Long stored = document.getLong("progressPercent");
                            int progress = stored == null ? 0 : clamp(stored.intValue());
                            values.put(document.getId(), progress);
                        }
                    }
                    callback.onProgress(values);
                });
    }

    public void saveProgress(String lessonId, int progressPercent) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || uid.trim().isEmpty() || lessonId == null
                || lessonId.trim().isEmpty()) return;

        int requestedProgress = clamp(progressPercent);
        DocumentReference reference = firestore.collection("userFoundationProgress")
                .document(uid)
                .collection("grammar")
                .document(lessonId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reference);
            Long stored = snapshot.getLong("progressPercent");
            int progress = Math.max(stored == null ? 0 : stored.intValue(), requestedProgress);

            Map<String, Object> data = new HashMap<>();
            data.put("lessonId", lessonId);
            data.put("progressPercent", progress);
            data.put("completed", progress >= 100);
            data.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(reference, data, SetOptions.merge());
            return null;
        });
    }

    public void stop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
