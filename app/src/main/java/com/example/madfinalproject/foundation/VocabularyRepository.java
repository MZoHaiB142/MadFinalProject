package com.example.madfinalproject.foundation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class VocabularyRepository {
    public interface Callback {
        void onData(List<VocabularyWord> words);
        void onError(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    public void listen(Callback callback) {
        stop();
        listener = firestore.collection("englishFoundation")
                .document("vocabulary")
                .collection("words")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    List<VocabularyWord> words = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            VocabularyWord word = document.toObject(VocabularyWord.class);
                            if (word == null) continue;
                            if (word.id == null || word.id.trim().isEmpty()) {
                                word.id = document.getId();
                            }
                            words.add(word);
                        }
                    }
                    words.sort(Comparator.comparing(
                            value -> value.id == null ? "" : value.id
                    ));
                    callback.onData(words);
                });
    }

    public void stop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
