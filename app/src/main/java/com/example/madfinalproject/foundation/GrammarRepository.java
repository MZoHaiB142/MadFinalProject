package com.example.madfinalproject.foundation;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GrammarRepository {
    public interface Callback {
        void onData(GrammarCurriculumMeta meta, List<GrammarLesson> lessons);
        void onError(String message);
    }

    private final DatabaseReference reference = FirebaseDatabase.getInstance()
            .getReference("englishFoundation").child("grammar");
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ValueEventListener realtimeListener;
    private ListenerRegistration firestoreLessonsListener;
    private ListenerRegistration firestoreDocumentListener;
    private Callback activeCallback;

    public void listen(Callback callback) {
        stop();
        activeCallback = callback;
        listenToFirestoreLessons(callback);
    }

    private void listenToFirestoreLessons(Callback callback) {
        firestoreLessonsListener = firestore.collection("englishFoundation")
                .document("grammar")
                .collection("lessons")
                .orderBy("order")
                .addSnapshotListener((snapshot, error) -> {
                    if (activeCallback == null) return;
                    if (error != null) {
                        startLegacyFirestoreFallback(callback);
                        return;
                    }
                    List<GrammarLesson> lessons = mapLessons(snapshot);
                    if (!lessons.isEmpty()) {
                        callback.onData(createMeta(lessons.size()), lessons);
                        return;
                    }
                    startLegacyFirestoreFallback(callback);
                });
    }

    private List<GrammarLesson> mapLessons(QuerySnapshot snapshot) {
        List<GrammarLesson> lessons = new ArrayList<>();
        if (snapshot == null) return lessons;
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            GrammarLesson lesson = document.toObject(GrammarLesson.class);
            if (lesson == null) continue;
            if (lesson.id == null || lesson.id.trim().isEmpty()) {
                lesson.id = document.getId();
            }
            lessons.add(lesson);
        }
        lessons.sort(Comparator.comparingInt(value -> value.order));
        return lessons;
    }

    private GrammarCurriculumMeta createMeta(int lessonCount) {
        GrammarCurriculumMeta meta = new GrammarCurriculumMeta();
        meta.curriculum = "AbroadIQ Grammar";
        meta.totalLessonsInFile = lessonCount;
        meta.targetTotalLessons = lessonCount;
        return meta;
    }

    private void startLegacyFirestoreFallback(Callback callback) {
        if (firestoreDocumentListener != null || activeCallback == null) return;
        firestoreDocumentListener = firestore.collection("englishFoundation")
                .document("grammar")
                .addSnapshotListener((snapshot, error) -> {
                    if (activeCallback == null) return;
                    if (error != null) {
                        startRealtimeFallback(callback);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        CurriculumDocument document = snapshot.toObject(CurriculumDocument.class);
                        GrammarCurriculumMeta meta = document == null || document.meta == null
                                ? new GrammarCurriculumMeta() : document.meta;
                        List<GrammarLesson> lessons = document == null || document.lessons == null
                                ? new ArrayList<>() : new ArrayList<>(document.lessons);
                        lessons.sort(Comparator.comparingInt(value -> value.order));
                        if (!lessons.isEmpty()) {
                            callback.onData(meta, lessons);
                            return;
                        }
                    }
                    startRealtimeFallback(callback);
                });
    }

    private void startRealtimeFallback(Callback callback) {
        if (realtimeListener != null || activeCallback == null) return;
        realtimeListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GrammarCurriculumMeta meta = snapshot.child("meta")
                        .getValue(GrammarCurriculumMeta.class);
                if (meta == null) meta = new GrammarCurriculumMeta();

                List<GrammarLesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.child("lessons").getChildren()) {
                    GrammarLesson lesson = child.getValue(GrammarLesson.class);
                    if (lesson != null) lessons.add(lesson);
                }
                lessons.sort(Comparator.comparingInt(value -> value.order));
                callback.onData(meta, lessons);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void stop() {
        activeCallback = null;
        if (firestoreLessonsListener != null) {
            firestoreLessonsListener.remove();
            firestoreLessonsListener = null;
        }
        if (firestoreDocumentListener != null) {
            firestoreDocumentListener.remove();
            firestoreDocumentListener = null;
        }
        if (realtimeListener != null) {
            reference.removeEventListener(realtimeListener);
            realtimeListener = null;
        }
    }

    public static class CurriculumDocument {
        public GrammarCurriculumMeta meta;
        public List<GrammarLesson> lessons;
        public CurriculumDocument() {}
    }
}
