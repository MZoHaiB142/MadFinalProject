package com.example.madfinalproject;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    // --- VARIABLES ---
    private int currentStep = 1;

    // Layout Containers
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;

    // Sidebar Items
    private ImageView iconStep1, iconStep2, iconStep3;
    private TextView txtStep1, txtStep2, txtStep3;
    private View viewLine1, viewLine2;
    private CardView cvStep1, cvStep2, cvStep3;

    // Buttons
    private MaterialButton btnNext, btnBack;

    // --- STEP 1 INPUTS ---
    private ImageView ivProfileUpload;
    private Uri imageUri;
    private EditText etFirstName, etLastName, etPhone, etCity;
    private Spinner spinnerCountry;

    // --- STEP 2 INPUTS (Education) ---
    private Spinner spinnerHighestQual;
    private CardView cvMasters, cvBachelors, cvInter, cvMatric;
    private EditText etMastersUni, etMastersCgpa;
    private EditText etBachelorsUni, etBachelorsCgpa;
    private EditText etInterInstitute, etInterMarks;
    private EditText etMatricInstitute, etMatricMarks;

    // --- STEP 3 INPUTS (Interests) ---
    private TextView tvMultiCountries, tvMultiFields;

    // Multi-Select Data
    private ArrayList<Integer> selectedCountryIndices = new ArrayList<>();
    private ArrayList<Integer> selectedFieldIndices = new ArrayList<>();

    // Arrays
    private final String[] countriesWithFlags = {"🇵🇰 Pakistan", "🇮🇳 India", "🇺🇸 USA", "🇬🇧 UK", "🇨🇦 Canada", "🇦🇺 Australia", "🇩🇪 Germany", "🇨🇳 China", "🇦🇪 UAE"};
    private final String[] qualifications = {"Select Qualification", "Matric / O-Levels", "Intermediate / A-Levels", "Bachelor's (BS)", "Master's (MS)", "PhD"};
    private final String[] targetCountriesList = {"USA", "UK", "Canada", "Australia", "Germany", "Italy", "France", "Sweden", "Turkey", "China", "Malaysia"};
    private final String[] studyFieldsList = {"Computer Science", "Software Engineering", "Business", "Medicine", "Engineering", "Arts & Design", "Law", "Data Science"};

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mStorage = FirebaseStorage.getInstance().getReference("ProfileImages");

        // 1. Initialize All Views
        initViews();

        // 2. Setup Spinners
        setupSpinners();

        // 3. Load Existing Data (Future Implementation)
        loadUserData();

        // 4. Update UI for Step 1
        updateUI();

        // --- CLICK LISTENERS ---

        // Next / Save Button
        btnNext.setOnClickListener(v -> {
            if (currentStep == 1 && validateStep1()) {
                currentStep = 2;
                updateUI();
            } else if (currentStep == 2 && validateStep2()) {
                currentStep = 3;
                updateUI();
            } else if (currentStep == 3 && validateStep3()) {
                // Final Step: Save Data
                saveDataToFirebase();
            }
        });

        // Back Button
        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateUI();
            }
        });

        // Image Upload
        ivProfileUpload.setOnClickListener(v -> pickImageFromGallery());

        // Multi-Select Dialogs
        tvMultiCountries.setOnClickListener(v -> showMultiSelectDialog("Select Target Countries", targetCountriesList, selectedCountryIndices, tvMultiCountries));
        tvMultiFields.setOnClickListener(v -> showMultiSelectDialog("Select Study Fields", studyFieldsList, selectedFieldIndices, tvMultiFields));
    }

    // --- INITIALIZATION ---
    private void initViews() {
        // Layouts
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        // Sidebar Elements
        iconStep1 = findViewById(R.id.iconStep1); txtStep1 = findViewById(R.id.txtStep1);
        iconStep2 = findViewById(R.id.iconStep2); txtStep2 = findViewById(R.id.txtStep2);
        iconStep3 = findViewById(R.id.iconStep3); txtStep3 = findViewById(R.id.txtStep3);
        viewLine1 = findViewById(R.id.viewLine1);
        viewLine2 = findViewById(R.id.viewLine2);
        cvStep1 = (CardView) iconStep1.getParent();
        cvStep2 = findViewById(R.id.cvStep2);
        cvStep3 = findViewById(R.id.cvStep3);

        // Buttons
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // Step 1 Inputs
        ivProfileUpload = findViewById(R.id.ivProfileUpload);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        spinnerCountry = findViewById(R.id.spinnerCountry);

        // Step 2 Inputs (Education)
        spinnerHighestQual = findViewById(R.id.spinnerHighestQual);

        cvMasters = findViewById(R.id.cvMastersDetails);
        cvBachelors = findViewById(R.id.cvBachelorsDetails);
        cvInter = findViewById(R.id.cvInterDetails);
        cvMatric = findViewById(R.id.cvMatricDetails);

        etMastersUni = findViewById(R.id.etMastersUni); etMastersCgpa = findViewById(R.id.etMastersCgpa);
        etBachelorsUni = findViewById(R.id.etBachelorsUni); etBachelorsCgpa = findViewById(R.id.etBachelorsCgpa);
        etInterInstitute = findViewById(R.id.etInterInstitute); etInterMarks = findViewById(R.id.etInterMarks);
        etMatricInstitute = findViewById(R.id.etMatricInstitute); etMatricMarks = findViewById(R.id.etMatricMarks);

        // Step 3 Inputs
        tvMultiCountries = findViewById(R.id.tvMultiCountries);
        tvMultiFields = findViewById(R.id.tvMultiFields);
    }

    private void setupSpinners() {
        // Country Adapter
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countriesWithFlags);
        spinnerCountry.setAdapter(countryAdapter);

        // Qualification Adapter
        ArrayAdapter<String> qualAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, qualifications);
        spinnerHighestQual.setAdapter(qualAdapter);

        spinnerHighestQual.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateEducationVisibility(qualifications[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // --- UI LOGIC ---
    private void updateUI() {
        // Visibility
        layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        // Buttons
        btnBack.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(currentStep == 3 ? "Save Profile" : "Next Step");

        // Sidebar Styling
        int activeColor = ContextCompat.getColor(this, R.color.brand_secondary);

        resetSidebarStyle(cvStep2, iconStep2, txtStep2);
        resetSidebarStyle(cvStep3, iconStep3, txtStep3);
        viewLine1.setBackgroundColor(Color.parseColor("#CCCCCC"));
        viewLine2.setBackgroundColor(Color.parseColor("#CCCCCC"));

        if (currentStep >= 2) {
            setSidebarActive(cvStep2, iconStep2, txtStep2);
            viewLine1.setBackgroundColor(activeColor);
        }
        if (currentStep >= 3) {
            setSidebarActive(cvStep3, iconStep3, txtStep3);
            viewLine2.setBackgroundColor(activeColor);
        }
    }

    private void setSidebarActive(CardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.brand_secondary));
        icon.setColorFilter(Color.WHITE);
        text.setTextColor(ContextCompat.getColor(this, R.color.brand_secondary));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetSidebarStyle(CardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(Color.WHITE);
        icon.setColorFilter(Color.parseColor("#AAAAAA"));
        text.setTextColor(Color.parseColor("#AAAAAA"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void updateEducationVisibility(String qual) {
        // Hide All
        if(cvMasters != null) cvMasters.setVisibility(View.GONE);
        if(cvBachelors != null) cvBachelors.setVisibility(View.GONE);
        if(cvInter != null) cvInter.setVisibility(View.GONE);
        if(cvMatric != null) cvMatric.setVisibility(View.GONE);

        // Show Based on Selection
        if (qual.contains("Master")) {
            cvMasters.setVisibility(View.VISIBLE); cvBachelors.setVisibility(View.VISIBLE); cvInter.setVisibility(View.VISIBLE); cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Bachelor")) {
            cvBachelors.setVisibility(View.VISIBLE); cvInter.setVisibility(View.VISIBLE); cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Inter")) {
            cvInter.setVisibility(View.VISIBLE); cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Matric")) {
            cvMatric.setVisibility(View.VISIBLE);
        }
    }

    // --- VALIDATION ---
    private boolean validateStep1() {
        if (TextUtils.isEmpty(etFirstName.getText())) { etFirstName.setError("Required"); return false; }
        if (TextUtils.isEmpty(etPhone.getText())) { etPhone.setError("Required"); return false; }
        return true;
    }
    private boolean validateStep2() {
        if (spinnerHighestQual.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select Qualification", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }
    private boolean validateStep3() {
        if (tvMultiCountries.getText().toString().contains("Tap to select")) {
            Toast.makeText(this, "Select at least 1 country", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }

    // --- IMAGE PICKER ---
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfileUpload.setImageURI(imageUri);
                }
            }
    );

    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    // --- MULTI-SELECT ---
    private void showMultiSelectDialog(String title, String[] items, ArrayList<Integer> selectedItems, TextView targetView) {
        boolean[] checkedItems = new boolean[items.length];
        for (int i : selectedItems) checkedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) selectedItems.add(which);
            else selectedItems.remove(Integer.valueOf(which));
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder sb = new StringBuilder();
            Collections.sort(selectedItems);
            for (int i = 0; i < selectedItems.size(); i++) {
                sb.append(items[selectedItems.get(i)]);
                if (i != selectedItems.size() - 1) sb.append(", ");
            }
            targetView.setText(sb.toString());
        });
        builder.show();
    }

    // --- FIREBASE SAVING (FIXED LOGIC) ---
    private void saveDataToFirebase() {
        Toast.makeText(this, "Saving Profile...", Toast.LENGTH_SHORT).show();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        if (imageUri != null) {
            StorageReference fileRef = mStorage.child(uid + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDetailsToDatabase(uid, uri.toString());
                    })
            ).addOnFailureListener(e -> Toast.makeText(this, "Image Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            saveDetailsToDatabase(uid, null);
        }
    }

    private void saveDetailsToDatabase(String uid, String imageUrl) {
        Map<String, Object> map = new HashMap<>();

        // 1. Personal
        map.put("fullName", etFirstName.getText().toString() + " " + etLastName.getText().toString());
        map.put("firstName", etFirstName.getText().toString());
        map.put("phone", etPhone.getText().toString());
        map.put("city", etCity.getText().toString());
        map.put("country", spinnerCountry.getSelectedItem().toString());
        if (imageUrl != null) map.put("profileImage", imageUrl);

        // 2. Interests
        map.put("targetCountries", tvMultiCountries.getText().toString());
        map.put("interestedFields", tvMultiFields.getText().toString());

        // 3. Education (Logic Added)
        String qual = spinnerHighestQual.getSelectedItem().toString();
        map.put("qualification", qual);

        if (qual.contains("Master")) {
            map.put("universityName", etMastersUni.getText().toString());
            map.put("lastGrades", etMastersCgpa.getText().toString());
        } else if (qual.contains("Bachelor")) {
            map.put("universityName", etBachelorsUni.getText().toString());
            map.put("lastGrades", etBachelorsCgpa.getText().toString());
        } else if (qual.contains("Inter")) {
            map.put("collegeName", etInterInstitute.getText().toString());
            map.put("lastGrades", etInterMarks.getText().toString());
        } else if (qual.contains("Matric")) {
            map.put("schoolName", etMatricInstitute.getText().toString());
            map.put("lastGrades", etMatricMarks.getText().toString());
        }

        // 4. Update Database
        mDatabase.child(uid).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
                // Navigate back to Profile Activity
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Save Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        // Future: Add logic to fetch and set existing data
    }
}