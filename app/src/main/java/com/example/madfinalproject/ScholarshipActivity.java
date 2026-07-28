package com.example.madfinalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Import check karna
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.ScholarshipAdapter;
import com.example.madfinalproject.models.ScholarshipModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.scholarships.FavoriteScholarshipRepository;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScholarshipActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ScholarshipAdapter adapter;
    List<ScholarshipModel> fullList; // Sara Data yahan hoga
    List<ScholarshipModel> filterList; // Filter kiya hua data yahan hoga
    List<ScholarshipModel> eligibleList;
    FirebaseFirestore db;

    // UI Elements
    SearchView searchView;
    Button btnAll, btnFull, btnPartial, btnEurope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scholarship);

        // Init UI
        recyclerView = findViewById(R.id.recyclerViewScholarships);
        searchView = findViewById(R.id.searchView);
        btnAll = findViewById(R.id.btnFilterAll);
        btnFull = findViewById(R.id.btnFilterFull);
        btnPartial = findViewById(R.id.btnFilterPartial);
        btnEurope = findViewById(R.id.btnFilterEurope);

        // Setup List
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fullList = new ArrayList<>();
        filterList = new ArrayList<>();
        eligibleList = new ArrayList<>();
        adapter = new ScholarshipAdapter(this, filterList); // Adapter ko filter list denge
        recyclerView.setAdapter(adapter);
        new FavoriteScholarshipRepository().load(ids -> adapter.setFavouriteIds(ids));
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 501);
        }

        // Load Data
        db = FirebaseFirestore.getInstance();
        fetchScholarships();

        // --- SEARCH LOGIC ---
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText, "All");
                return true;
            }
        });

        // --- BUTTON FILTER LOGIC ---
        btnAll.setOnClickListener(v -> { highlightBtn(btnAll); filterData("", "All"); });
        btnFull.setOnClickListener(v -> { highlightBtn(btnFull); filterData("Fully Funded", "Tag"); });
        btnPartial.setOnClickListener(v -> { highlightBtn(btnPartial); filterData("Partially", "Tag"); });
        btnEurope.setOnClickListener(v -> { highlightBtn(btnEurope); filterData("Europe", "Country"); });
    }

    // 🔥 REAL DATA FETCH
    private void fetchScholarships() {
        db.collectionGroup("data").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                fullList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ScholarshipModel model = doc.toObject(ScholarshipModel.class);
                    model.setId(doc.getId());
                    model.setTargetDegree(readDegreeLevel(doc.getData(), model));
                    fullList.add(model);
                }
                loadProfileAndPersonalize();
            }
        }).addOnFailureListener(error -> android.widget.Toast.makeText(this,
                "Scholarships could not be loaded. Please check your connection.", android.widget.Toast.LENGTH_LONG).show());
    }

    private void loadProfileAndPersonalize() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showRandomScholarships();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(Constants.DB_USERS).document(uid).get()
                .addOnSuccessListener(profile -> {
                    String qualification = profile.getString(Constants.KEY_QUALIFICATION);
                    String targetDegree = targetDegreeForQualification(qualification);
                    if (targetDegree.isEmpty()) showRandomScholarships();
                    else showPersonalizedScholarships(targetDegree);
                })
                .addOnFailureListener(error -> showRandomScholarships());
    }

    private void showPersonalizedScholarships(String targetDegree) {
        eligibleList.clear();
        List<ScholarshipModel> general = new ArrayList<>();
        for (ScholarshipModel item : fullList) {
            String level = normalize(item.getTargetDegree());
            if (level.isEmpty()) general.add(item);
            else if (degreeMatches(level, targetDegree)) eligibleList.add(item);
        }
        Collections.shuffle(eligibleList);
        Collections.shuffle(general);
        int generalLimit = Math.min(5, general.size());
        eligibleList.addAll(general.subList(0, generalLimit));
        if (eligibleList.isEmpty()) {
            eligibleList.addAll(fullList);
            Collections.shuffle(eligibleList);
        }
        displayBaseList();
        android.widget.Toast.makeText(this, "Showing " + targetDegree + " scholarships for your profile",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showRandomScholarships() {
        eligibleList.clear();
        eligibleList.addAll(fullList);
        Collections.shuffle(eligibleList);
        displayBaseList();
    }

    private void displayBaseList() {
        filterList.clear();
        filterList.addAll(eligibleList);
        adapter.notifyDataSetChanged();
    }

    // 🔍 SEARCH & FILTER ENGINE
    private void filterData(String query, String type) {
        List<ScholarshipModel> tempList = new ArrayList<>();

        for (ScholarshipModel item : eligibleList) {
            boolean match = false;

            if (type.equals("All")) {
                // Search in Title or Country
                if (safe(item.getTitle()).toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US)) ||
                        safe(item.getCountry()).toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US))) {
                    match = true;
                }
            } else if (type.equals("Tag")) {
                // Search in Amount/Funding
                if (item.getAmount() != null && item.getAmount().contains(query)) {
                    match = true;
                }
            } else if (type.equals("Country")) {
                // Search in Country
                if (safe(item.getCountry()).contains(query) || safe(item.getSource()).contains("OpportunitiesRadar")) {
                    match = true;
                }
            }

            if (match) tempList.add(item);
        }

        // Update Adapter
        filterList.clear();
        filterList.addAll(tempList);
        adapter.notifyDataSetChanged();
    }

    private String targetDegreeForQualification(String qualification) {
        String value = normalize(qualification);
        if (value.isEmpty() || value.contains("select qualification")) return "";
        if (value.contains("matric") || value.contains("o-level")) return "Intermediate";
        if (value.contains("intermediate") || value.contains("a-level")) return "Bachelor";
        if (value.contains("bachelor") || value.contains("bs")) return "Master";
        if (value.contains("master") || value.contains("ms")) return "PhD";
        if (value.equals("phd") || value.contains("doctor")) return "Postdoctoral";
        return "";
    }

    private String readDegreeLevel(Map<String, Object> data, ScholarshipModel model) {
        String[] keys = {"degreeLevel", "degree_level", "studyLevel", "study_level",
                "programLevel", "program_level", "eligibleDegree", "eligible_degree", "qualification"};
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) return String.valueOf(value);
        }
        return inferDegreeFromText(safe(model.getTitle()) + " " + safe(model.getUniversity()));
    }

    private String inferDegreeFromText(String text) {
        String value = normalize(text);
        if (value.contains("postdoc")) return "Postdoctoral";
        if (value.contains("phd") || value.contains("doctoral")) return "PhD";
        if (value.contains("bachelor") || value.contains("undergraduate")) return "Bachelor";
        if (value.contains("master") || value.contains("postgraduate") || value.equals("graduate")) return "Master";
        if (value.contains("intermediate") || value.contains("high school")) return "Intermediate";
        return "";
    }

    private boolean degreeMatches(String storedLevel, String targetDegree) {
        String stored = normalize(storedLevel);
        String target = normalize(targetDegree);
        if (target.equals("bachelor")) return stored.contains("bachelor") || stored.contains("undergraduate");
        if (target.equals("master")) return stored.contains("master") || stored.contains("postgraduate") || stored.equals("graduate");
        if (target.equals("phd")) return stored.contains("phd") || stored.contains("doctoral");
        if (target.equals("postdoctoral")) return stored.contains("postdoc");
        return stored.contains(target);
    }

    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.US); }
    private String safe(String value) { return value == null ? "" : value; }

    // Button Color Logic (Optional UX)
    private void highlightBtn(Button active) {
        Button[] buttons = {btnAll, btnFull, btnPartial, btnEurope};
        for (Button btn : buttons) {
            btn.setBackgroundColor(Color.parseColor(btn == active ? "#2196F3" : "#E0E0E0"));
            btn.setTextColor(Color.parseColor(btn == active ? "#FFFFFF" : "#000000"));
        }
    }
}
