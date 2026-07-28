package com.example.madfinalproject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madfinalproject.models.GeneratedSop;
import com.example.madfinalproject.repository.SopRepository;
import com.google.firebase.firestore.ListenerRegistration;

public class SopResultActivity extends AppCompatActivity {

    // Views — existing XML ke saath match karte hain
    private ImageView  ivBell, ivProfile;
    private Button     btnEdit, btnRegenerate, btnDownload;
    private TextView   tvSopText, tvScore, tvWordCount;
    private TextView   tvMotivation, tvVisaStrength, tvClarity, tvCareerAlign;
    private ProgressBar progressScore;
    private View       loadingOverlay;

    // Data
    private String sopText     = "";
    private String requestId   = "";
    private String countryName = "";

    // Repository
    private SopRepository repository;
    private ListenerRegistration listenerReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sop_result);

        repository = new SopRepository();

        initViews();
        loadDataFromIntent();
        setupButtons();
    }

    private void initViews() {
        ivBell         = findViewById(R.id.iv_bell);
        ivProfile      = findViewById(R.id.iv_profile);
        btnEdit        = findViewById(R.id.btn_edit);
        btnRegenerate  = findViewById(R.id.btn_regenerate);
        btnDownload    = findViewById(R.id.btn_download);

        // These IDs need to be in sop_result.xml
        // Existing static TextViews ke jagah dynamic ones
        tvSopText      = findViewById(R.id.tv_sop_content);
        tvScore        = findViewById(R.id.tv_score);
        tvWordCount    = findViewById(R.id.tv_word_count);
        progressScore  = findViewById(R.id.progress_bar);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        sopText      = intent.getStringExtra("SOP_TEXT");
        requestId    = intent.getStringExtra("REQUEST_ID");
        countryName  = intent.getStringExtra("COUNTRY_NAME");
        double score       = intent.getDoubleExtra("SOP_SCORE", 0);
        int    wordCount   = intent.getIntExtra("WORD_COUNT", 0);
        double motivation  = intent.getDoubleExtra("MOTIVATION_SCORE", 0);
        double visa        = intent.getDoubleExtra("VISA_STRENGTH", 0);
        double clarity     = intent.getDoubleExtra("CLARITY_SCORE", 0);
        double career      = intent.getDoubleExtra("CAREER_ALIGN_SCORE", 0);

        // SOP text display karo
        if (sopText != null && !sopText.isEmpty()) {
            showSopContent(sopText, score, wordCount,
                    motivation, visa, clarity, career);
        } else if (requestId != null && !requestId.isEmpty()) {
            // Realtime listener — agar SOP abhi generate ho raha hai
            listenForSopResult();
        }
    }

    // ─────────────────────────────────────────────
    // SOP Content Display
    // ─────────────────────────────────────────────
    private void showSopContent(
            String sopText, double score, int wordCount,
            double motivation, double visa, double clarity, double career
    ) {
        if (tvSopText != null) {
            tvSopText.setText(sopText);
        }
        if (tvScore != null) {
            tvScore.setText(String.format("%.1f/10", score));
        }
        if (tvWordCount != null) {
            tvWordCount.setText(String.valueOf(wordCount));
        }
        if (progressScore != null) {
            progressScore.setProgress((int)(score * 10));
        }

        // Score cards update (agar XML mein hain)
        updateScoreCard(R.id.tv_motivation_score,   motivation);
        updateScoreCard(R.id.tv_visa_strength,      visa);
        updateScoreCard(R.id.tv_clarity_score,      clarity);
        updateScoreCard(R.id.tv_career_align_score, career);
    }

    private void updateScoreCard(int viewId, double value) {
        try {
            TextView tv = findViewById(viewId);
            if (tv != null) {
                tv.setText(String.format("%.1f", value));
            }
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────
    // Realtime Listener — result ka wait karo
    // ─────────────────────────────────────────────
    private void listenForSopResult() {
        listenerReg = repository.listenForSop(requestId,
                new SopRepository.SopListenerCallback() {

                    @Override
                    public void onUpdate(GeneratedSop sop) {
                        runOnUiThread(() -> {
                            if ("completed".equals(sop.getStatus())) {
                                showSopContent(
                                        sop.getSopText(),
                                        sop.getScore(),
                                        sop.getWordCount(),
                                        sop.getMotivationScore(),
                                        sop.getVisaStrengthScore(),
                                        sop.getClarityScore(),
                                        sop.getCareerAlignmentScore()
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(SopResultActivity.this,
                                        "Error: " + error, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────
    private void setupButtons() {

        // Copy to clipboard
        btnEdit.setOnClickListener(v -> {
            if (sopText != null && !sopText.isEmpty()) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(
                        ClipData.newPlainText("SOP", sopText));
                Toast.makeText(this, "SOP copied!", Toast.LENGTH_SHORT).show();
            }
        });

        // Regenerate
        btnRegenerate.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Regenerating...", Toast.LENGTH_SHORT).show();
            finish(); // Form par wapis jao
        });

        // Download PDF — placeholder
        btnDownload.setOnClickListener(v -> {
            Toast.makeText(this,
                    "PDF download — coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Listener remove karo — memory leak avoid karo
        if (listenerReg != null) {
            listenerReg.remove();
        }
    }
}