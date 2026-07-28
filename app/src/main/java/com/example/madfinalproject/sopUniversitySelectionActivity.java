package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.SopUniversityAdapter;
import com.example.madfinalproject.models.SopUniversity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class sopUniversitySelectionActivity extends AppCompatActivity {

    private Button btn_next, btn_back;
    private RecyclerView rvUniversities;
    private TextView tvSubtitle;

    private FirebaseFirestore db;
    private SopUniversityAdapter adapter;
    private List<SopUniversity> universityList;
    private String receivedCountry = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sop_university_selection);

        // 1. XML design ke views ko idhar link karna
        btn_next = findViewById(R.id.btn_next);
        btn_back = findViewById(R.id.btn_back);
        rvUniversities = findViewById(R.id.rv_universities);
        tvSubtitle = findViewById(R.id.tv_subtitle);

        // 2. Pichli screen se country ka naam receive karna
        receivedCountry = getIntent().getStringExtra("SELECTED_COUNTRY");
        if (receivedCountry == null || receivedCountry.isEmpty()) {
            receivedCountry = "USA"; // Agar data na aaye toh fallback
        }

        // Subtitle update karna (e.g. "Step 2: Choose your institution in Australia")
        tvSubtitle.setText("Step 2: Choose your institution in " + receivedCountry);

        // 3. RecyclerView aur Adapter Setup karna (List banane ka zaroori hissa)
        rvUniversities.setLayoutManager(new LinearLayoutManager(this));
        universityList = new ArrayList<>();
        adapter = new SopUniversityAdapter(this, universityList);
        rvUniversities.setAdapter(adapter);

        // 4. Firebase se Data Mangwana
        fetchUniversitiesFromFirebase();

        // 5. Back Button ka action
        btn_back.setOnClickListener(v -> finish()); // Pichli screen par wapis jane ke liye

        // 6. Next Button ka Sahi Action (Form wale page par bhejna)
        btn_next.setOnClickListener(v -> {
            SopUniversity selectedUni = adapter.getSelectedUniversity();

            if (selectedUni != null) {
                // Yahan se user detail wale form par jane ka Intent
                Intent intent = new Intent(sopUniversitySelectionActivity.this, UserDetailsActivity.class);

                // Data sath bhejein
                intent.putExtra("UNIVERSITY_NAME", selectedUni.getName());
                intent.putExtra("COUNTRY_NAME", receivedCountry);

                // Agli screen khol dain
                startActivity(intent);

            } else {
                // Agar user ne koi uni select nahi ki toh error dikhayein
                Toast.makeText(this, "Please select a university first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🌍 YEH FUNCTION FIREBASE SE DATA LATA HAI
    private void fetchUniversitiesFromFirebase() {
        db = FirebaseFirestore.getInstance();

        // Aapki collection 'sopuniversities' se data filter kar ke lana
        db.collection("sopuniversities")
                .whereEqualTo("country", receivedCountry)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        universityList.clear(); // List ko pehle khali karna

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Firebase ke data ko SopUniversity model mein daalna
                            SopUniversity uni = document.toObject(SopUniversity.class);
                            universityList.add(uni);
                        }

                        // Adapter ko batana ke naya data aa gaya hai, list screen par dikhao
                        adapter.notifyDataSetChanged();

                        if(universityList.isEmpty()) {
                            Toast.makeText(this, "No universities found for " + receivedCountry, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}