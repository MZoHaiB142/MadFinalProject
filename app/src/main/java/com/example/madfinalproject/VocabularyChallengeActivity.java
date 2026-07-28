package com.example.madfinalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.madfinalproject.foundation.VocabularyChallengeRepository;

public class VocabularyChallengeActivity extends VocabularyBaseActivity {
    private final VocabularyChallengeRepository challengeRepository =
            new VocabularyChallengeRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_challenge);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnStartChallenge).setOnClickListener(
                view -> open(VocabularyFlashcardsActivity.class));
        challengeRepository.listenToday(new VocabularyChallengeRepository.Callback() {
            public void onChallenge(VocabularyChallengeRepository.DailyChallenge challenge) {
                renderChallenge(challenge);
            }
            public void onError(String message) {
                ((TextView) findViewById(R.id.challengeReward))
                        .setText("Challenge progress could not be synced.");
            }
        });
        listenForVocabulary();
    }

    @Override
    protected void onVocabularyUpdated() {}

    private void renderChallenge(VocabularyChallengeRepository.DailyChallenge challenge) {
        int learned = challenge.learnedWordIds == null ? 0 : challenge.learnedWordIds.size();
        setTask(R.id.challengeWords, learned >= 10,
                "Learn 10 new words (" + Math.min(learned, 10) + "/10)");
        setTask(R.id.challengeQuiz, challenge.quizCompleted, "Complete a quiz");
        setTask(R.id.challengeSpeaking, challenge.speakingCompleted, "Practice speaking");
        setTask(R.id.challengeScore, challenge.bestScore >= 90,
                "Score 90% or above (" + challenge.bestScore + "%)");
        int completed = challenge.completedTasks();
        ((ProgressBar) findViewById(R.id.challengeProgress)).setProgress(completed * 25, true);
        TextView reward = findViewById(R.id.challengeReward);
        reward.setText(completed == 4
                ? "Reward unlocked: +50 XP and +1 Achievement Badge"
                : "+50 XP     •     +1 Achievement Badge");
        ((android.widget.Button) findViewById(R.id.btnStartChallenge))
                .setText(completed == 4 ? "Challenge Completed" : "Continue Challenge");
    }

    private void setTask(int id, boolean complete, String label) {
        TextView view = findViewById(id);
        view.setText((complete ? "✓  " : "○  ") + label);
        view.setTextColor(Color.parseColor(complete ? "#16A34A" : "#34425D"));
    }

    @Override
    protected void onDestroy() {
        challengeRepository.stop();
        super.onDestroy();
    }
}
