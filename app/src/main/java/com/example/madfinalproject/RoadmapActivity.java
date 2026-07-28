package com.example.madfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.RoadmapAdapter;
import com.example.madfinalproject.models.Phase;
import com.example.madfinalproject.models.RoadmapStep;
import com.example.madfinalproject.models.SheetData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RoadmapActivity extends AppCompatActivity {

    private TextView tvRoute;
    private ImageView ivBack;
    private LinearLayout progressStrip, layoutLoading;
    private RecyclerView rvRoadmap;

    private RoadmapAdapter adapter;
    private FirebaseFirestore db;

    private String countryId = "";
    private String countryName = "";
    private String countryFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);

        db = FirebaseFirestore.getInstance();

        // 1. Find Views
        tvRoute = findViewById(R.id.tv_route);
        ivBack = findViewById(R.id.iv_back);
        progressStrip = findViewById(R.id.progress_strip);
        layoutLoading = findViewById(R.id.layout_loading);
        rvRoadmap = findViewById(R.id.rv_roadmap);

        // 2. Get Data from Intent
        if (getIntent() != null) {
            countryId = getIntent().getStringExtra("COUNTRY_ID");
            countryName = getIntent().getStringExtra("COUNTRY_NAME");
            countryFlag = getIntent().getStringExtra("COUNTRY_FLAG");

            if (countryId == null) countryId = "";
            if (countryName == null) countryName = "";
            if (countryFlag == null) countryFlag = "";
        }

        // 3. Set Topbar Text
        tvRoute.setText("🇵🇰 Pakistan → " + countryFlag + " " + countryName);

        // 4. Back Button Click
        ivBack.setOnClickListener(v -> finish());

        // 5. Setup List & Load Data
        setupRecyclerView();
        loadRoadmap();
    }

    private void setupRecyclerView() {
        // Yeh adapter hum pichle step mein bana chuke hain
        adapter = new RoadmapAdapter(this, sheetType -> loadAndShowSheet(sheetType));
        rvRoadmap.setLayoutManager(new LinearLayoutManager(this));
        rvRoadmap.setAdapter(adapter);
    }

    // 🔥 Firebase se phases aur steps load karna
    private void loadRoadmap() {
        Toast.makeText(this, "Firebase ID checking: [" + countryId + "]", Toast.LENGTH_LONG).show();
        if (countryId.isEmpty()) return; // Agar country empty ho toh data load na kare
        showLoading(true);
        db.collection("countries").document(countryId)
                .collection("phases")
                .orderBy("order")
                .get()
                .addOnSuccessListener(phaseSnap -> {
                    List<Phase> phases = new ArrayList<>();
                    for (QueryDocumentSnapshot d : phaseSnap) {
                        Phase phase = d.toObject(Phase.class);
                        phase.id = d.getId();
                        phases.add(phase);
                    }
                    db.collection("countries").document(countryId)
                            .collection("steps")
                            .orderBy("order")
                            .get()
                            .addOnSuccessListener(stepSnap -> {
                                List<RoadmapStep> steps = new ArrayList<>();
                                for (QueryDocumentSnapshot d : stepSnap) {
                                    RoadmapStep step = d.toObject(RoadmapStep.class);
                                    step.id = d.getId();
                                    steps.add(step);
                                }

                                List<RoadmapAdapter.RoadmapItem> items = buildRoadmapItems(phases, steps);
                                adapter.submitList(items);
                                buildProgressStrip(steps.size());
                                showLoading(false);
                            })
                            .addOnFailureListener(e -> showLoading(false));
                })
                .addOnFailureListener(e -> showLoading(false));
    }


    private List<RoadmapAdapter.RoadmapItem> buildRoadmapItems(List<Phase> phases, List<RoadmapStep> steps) {
        List<RoadmapAdapter.RoadmapItem> items = new ArrayList<>();

        for (Phase phase : phases) {
            items.add(new RoadmapAdapter.PhaseItem(phase)); // Pehle Phase ki heading

            for (RoadmapStep step : steps) {
                // Agar step ka phase_id is current phase se match karta hai toh iske neechay add karo
                if (step.phase_id != null && step.phase_id.equals(phase.id)) {
                    items.add(new RoadmapAdapter.StepItem(step));
                }
            }
        }
        return items;
    }

    // Firebase se Sheet data lana aur popup (BottomSheet) dikhana
    private void loadAndShowSheet(String sheetType) {
        if (sheetType == null || sheetType.equals("none") || sheetType.isEmpty()) return;

        db.collection("sheet_data").document(sheetType)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SheetData sheetData = doc.toObject(SheetData.class);
                        if (sheetData != null) {
                            showBottomSheet(sheetData);
                        }
                    } else {
                        // Agar firebase mein ye sheet nahi bani hui toh testing ke liye ek dummy sheet khol do
                        Toast.makeText(this, "Sheet Data missing in Firebase. Add 'sheet_data' collection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showBottomSheet(SheetData data) {
        // Note: GuideBottomSheet class abhi humne properly java mein nahi banayi hai,
        // isliye abhi ke liye sirf toast dikhate hain agar aapne wo class nahi banayi.
        // Agar banayi hui hai toh neechay wali 2 lines uncomment kar lein:

        // GuideBottomSheet sheet = GuideBottomSheet.newInstance(data);
        // sheet.show(getSupportFragmentManager(), "guide_sheet");

        Toast.makeText(this, "Opening Sheet: " + data.title, Toast.LENGTH_SHORT).show();
    }

    // Top par choti choti progress ki lines (bars) banana
    private void buildProgressStrip(int totalSteps) {
        progressStrip.removeAllViews();
        for (int i = 0; i < totalSteps; i++) {
            View seg = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, 12 // Height of progress bar (e.g. 12px or 4dp)
            );
            params.weight = 1f;
            params.setMarginEnd(8); // spacing
            seg.setLayoutParams(params);

            // Set color based on index
            if (i == 0) {
                seg.setBackgroundColor(android.graphics.Color.parseColor("#22C55E")); // Done (Green)
            } else if (i == 1) {
                seg.setBackgroundColor(android.graphics.Color.parseColor("#1B6DF9")); // Active (Blue)
            } else {
                seg.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0")); // Inactive (Grey)
            }
            progressStrip.addView(seg);
        }
    }

    private void showLoading(boolean show) {
        if (layoutLoading != null) layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvRoadmap != null) rvRoadmap.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
