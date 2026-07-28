package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ConsultantCallActivity extends AppCompatActivity {

    // Views
    private ImageView  ivCallingPhoto, ivCallOnlineDot;
    private TextView   tvCallingName, tvCallingExpertise;
    private TextView   tvCallingRating, tvConsultantPhone;
    private TextView   tvCallingLabel, tvCallDuration;
    private ImageButton btnStartCall, btnCancelCall, btnBackCall;
    private View       viewPulse;

    // Data
    private String consultantId;
    private String consultantName;
    private String consultantPhone;
    private String consultantPhoto;
    private String consultantExpertise;
    private double consultantRating;
    private boolean isOnline;

    // Firebase — online status realtime
    private FirebaseFirestore    db;
    private ListenerRegistration onlineListener;

    // Call timer
    private Handler  timerHandler;
    private Runnable timerRunnable;
    private int      secondsElapsed = 0;
    private boolean  callActive     = false;

    // Pulse animation handler
    private Handler  pulseHandler;
    private Runnable pulseRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        db = FirebaseFirestore.getInstance();

        // Intent data receive karo
        consultantId        = getIntent().getStringExtra("CONSULTANT_ID");
        consultantName      = getIntent().getStringExtra("CONSULTANT_NAME");
        consultantPhone     = getIntent().getStringExtra("CONSULTANT_PHONE");
        consultantPhoto     = getIntent().getStringExtra("CONSULTANT_PHOTO");
        consultantExpertise = getIntent().getStringExtra("CONSULTANT_EXPERTISE");
        consultantRating    = getIntent().getDoubleExtra("CONSULTANT_RATING", 0.0);
        isOnline            = getIntent().getBooleanExtra("IS_ONLINE", false);

        initViews();
        populateUI();
        setupButtons();
        startPulseAnimation();

        // Firebase se realtime online status
        listenForOnlineStatus();
    }

    // ─────────────────────────────────────────────
    // INIT VIEWS
    // ─────────────────────────────────────────────
    @SuppressLint("WrongViewCast")
    private void initViews() {
        ivCallingPhoto      = findViewById(R.id.ivCallingPhoto);
        ivCallOnlineDot     = findViewById(R.id.ivCallOnlineDot);
        tvCallingName       = findViewById(R.id.tvCallingName);
        tvCallingExpertise  = findViewById(R.id.tvCallingExpertise);
        tvCallingRating     = findViewById(R.id.tvCallingRating);
        tvConsultantPhone   = findViewById(R.id.tvConsultantPhone);
        tvCallingLabel      = findViewById(R.id.tvCallingLabel);
        tvCallDuration      = findViewById(R.id.tvCallDuration);
        btnStartCall        = findViewById(R.id.btnStartCall);
        btnCancelCall       = findViewById(R.id.btnCancelCall);
        btnBackCall         = findViewById(R.id.btnBackCall);
        viewPulse           = findViewById(R.id.viewPulse);
    }

    // ─────────────────────────────────────────────
    // POPULATE UI — Intent data se
    // ─────────────────────────────────────────────
    private void populateUI() {
        // Name
        tvCallingName.setText(
                consultantName != null ? consultantName : "Consultant");

        // Expertise
        tvCallingExpertise.setText(
                consultantExpertise != null ? consultantExpertise : "");

        // Rating
        if (consultantRating > 0) {
            tvCallingRating.setText(String.valueOf(consultantRating));
        }

        // Phone
        tvConsultantPhone.setText(
                consultantPhone != null ? consultantPhone : "Number not available");

        // Online dot
        ivCallOnlineDot.setVisibility(isOnline ? View.VISIBLE : View.GONE);

        // Profile photo — Glide se load
        if (consultantPhoto != null && !consultantPhoto.isEmpty()) {
            Glide.with(this)
                    .load(consultantPhoto)
                    .circleCrop()
                    .placeholder(R.drawable.ic_university)
                    .error(R.drawable.ic_university)
                    .into(ivCallingPhoto);
        }
    }

    // ─────────────────────────────────────────────
    // BUTTONS
    // ─────────────────────────────────────────────
    private void setupButtons() {

        // Back button
        btnBackCall.setOnClickListener(v -> finish());

        // Cancel call
        btnCancelCall.setOnClickListener(v -> {
            stopCallTimer();
            finish();
        });

        // Start Call — phone dialer open karo
        btnStartCall.setOnClickListener(v -> {
            if (!isOnline) {
                Toast.makeText(this,
                        consultantName + " abhi offline hai. "
                                + "Please try again later.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (consultantPhone == null || consultantPhone.isEmpty()) {
                Toast.makeText(this,
                        "Phone number is unavailable",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Phone dialer open karo
            makePhoneCall(consultantPhone);
        });
    }

    // ─────────────────────────────────────────────
    // PHONE CALL
    // ─────────────────────────────────────────────
    private void makePhoneCall(String phoneNumber) {
        try {
            // Label update
            tvCallingLabel.setText("Connecting...");
            callActive = true;

            // Phone number se whitespace hata do
            String cleanNumber = phoneNumber.replaceAll("\\s+", "");

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + cleanNumber));
            startActivity(callIntent);

            // Timer start karo
            startCallTimer();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to place the call: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────
    // CALL TIMER — duration dikhao
    // ─────────────────────────────────────────────
    private void startCallTimer() {
        secondsElapsed = 0;
        tvCallDuration.setVisibility(View.VISIBLE);
        tvCallingLabel.setText("On Call");

        timerHandler  = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                int minutes = secondsElapsed / 60;
                int seconds = secondsElapsed % 60;
                tvCallDuration.setText(
                        String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopCallTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    // ─────────────────────────────────────────────
    // PULSE ANIMATION — photo ke around
    // ─────────────────────────────────────────────
    private void startPulseAnimation() {
        pulseHandler  = new Handler(Looper.getMainLooper());
        pulseRunnable = new Runnable() {
            @Override
            public void run() {
                ScaleAnimation pulse = new ScaleAnimation(
                        1.0f, 1.2f,   // X: 1x to 1.2x
                        1.0f, 1.2f,   // Y: 1x to 1.2x
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                pulse.setDuration(800);
                pulse.setRepeatMode(Animation.REVERSE);
                pulse.setRepeatCount(Animation.INFINITE);
                viewPulse.startAnimation(pulse);
            }
        };
        pulseHandler.postDelayed(pulseRunnable, 300);
    }

    private void stopPulseAnimation() {
        if (viewPulse != null) {
            viewPulse.clearAnimation();
        }
        if (pulseHandler != null && pulseRunnable != null) {
            pulseHandler.removeCallbacks(pulseRunnable);
        }
    }

    // ─────────────────────────────────────────────
    // REALTIME ONLINE STATUS
    // ─────────────────────────────────────────────
    private void listenForOnlineStatus() {
        if (consultantId == null || consultantId.isEmpty()) return;

        onlineListener = db.collection("consultants")
                .document(consultantId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;

                    Boolean online = doc.getBoolean("isOnline");
                    isOnline = online != null && online;

                    runOnUiThread(() -> {
                        ivCallOnlineDot.setVisibility(
                                isOnline ? View.VISIBLE : View.GONE);

                        // Agar call active na ho aur consultant offline ho jaye
                        if (!isOnline && !callActive) {
                            tvCallingLabel.setText("Offline");
                            btnStartCall.setAlpha(0.5f);
                        }
                    });
                });
    }

    // ─────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCallTimer();
        stopPulseAnimation();
        if (onlineListener != null) onlineListener.remove();
    }
}
