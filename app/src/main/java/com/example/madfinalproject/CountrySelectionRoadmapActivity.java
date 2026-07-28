package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.models.Country;
import com.example.madfinalproject.adapters.CountryAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CountrySelectionRoadmapActivity extends AppCompatActivity {

    private RecyclerView rvCountries;
    private Button btnNext;
    private LinearLayout layoutLoading;
    private ScrollView scrollContent;

    // ✅ FIX: Sahi class ka naam likha hai
    private CountryAdapter adapter;
    private FirebaseFirestore db;
    private Country selectedCountry = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap_country_selection);

        db = FirebaseFirestore.getInstance();

        rvCountries = findViewById(R.id.rv_countries);
        btnNext = findViewById(R.id.btn_next);
        layoutLoading = findViewById(R.id.layout_loading);
        scrollContent = findViewById(R.id.scroll_content);

        setupRecyclerView();
        loadCountries();
        setupButtons();
    }

    private void setupRecyclerView() {
        // ✅ FIX: Sahi adapter call kiya hai
        adapter = new CountryAdapter(country -> {
            selectedCountry = country;
            btnNext.setEnabled(true);
            btnNext.setBackgroundResource(R.drawable.bg_btn_gradient);
        });

        rvCountries.setLayoutManager(new GridLayoutManager(this, 2));
        rvCountries.setAdapter(adapter);
    }

    private void loadCountries() {
        showLoading(true);
        List<Country> list = new ArrayList<>();

        db.collection("countries")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Country country = doc.toObject(Country.class);
                        country.id = doc.getId();
                        list.add(country);
                    }
                    adapter.submitList(list);
                    showLoading(false);
                })
                .addOnFailureListener(e -> showLoading(false));
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (selectedCountry != null) {
                Intent intent = new Intent(this, RoadmapActivity.class);
                intent.putExtra("COUNTRY_ID", selectedCountry.id);
                intent.putExtra("COUNTRY_NAME", selectedCountry.name);
                intent.putExtra("COUNTRY_FLAG", selectedCountry.flag);
                startActivity(intent);
            }
        });
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}