package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class CountrySelectionActivity extends AppCompatActivity {

    // Variables
    private Button btnBack, btnNext;
    private CardView cvUsa, cvUk, cvCanada, cvAustralia, cvGermany;

    // User ki selection store karne ke liye
    private String selectedCountry = "";

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sop_country_selection); // Apne XML file ka naam yahan likhein

        // 1. Views Initialize Karein
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);

        cvUsa = findViewById(R.id.cv_usa);
        cvUk = findViewById(R.id.cv_uk);
        cvCanada = findViewById(R.id.cv_canada);
        cvAustralia = findViewById(R.id.cv_australia);
        cvGermany = findViewById(R.id.cv_germany);

        // ----------------------------------------------------
        // 🔙 BACK BUTTON LOGIC (Dashboard par wapis jana)
        // ----------------------------------------------------
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(CountrySelectionActivity.this, dashboardActivity.class);
            // FLAG_ACTIVITY_CLEAR_TOP purani activities ko close kar deta hai taake backstack clean rahe
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Current activity ko close kar dega
        });

        // ----------------------------------------------------
        // 🌍 COUNTRY SELECTION LOGIC
        // ----------------------------------------------------
        cvUsa.setOnClickListener(v -> selectCountry("USA"));
        cvUk.setOnClickListener(v -> selectCountry("UK"));
        cvCanada.setOnClickListener(v -> selectCountry("Canada"));
        cvAustralia.setOnClickListener(v -> selectCountry("Australia"));
        cvGermany.setOnClickListener(v -> selectCountry("Germany"));

        // ----------------------------------------------------
        // ➡️ NEXT BUTTON LOGIC (Agli screen par jana)
        // ----------------------------------------------------
        btnNext.setOnClickListener(v -> {
            if (selectedCountry.isEmpty()) {
                // Agar koi mulk select nahi kiya toh error dikhaye
                Toast.makeText(this, "Please select a country first!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Proceeding with: " + selectedCountry, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CountrySelectionActivity.this, sopUniversitySelectionActivity.class);
                intent.putExtra("SELECTED_COUNTRY", selectedCountry);
                startActivity(intent);
            }
        });
    }

    // Ek function jo click hone par selection save karega
    private void selectCountry(String countryName) {
        selectedCountry = countryName;

        // Abhi ke liye hum sirf ek Toast message dikha rahe hain
        Toast.makeText(this, "Selected: " + countryName, Toast.LENGTH_SHORT).show();
    }
}