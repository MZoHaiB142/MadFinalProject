package com.example.madfinalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.example.madfinalproject.utils.ValidationUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private ImageView ivProfileUpload, btnAutoLocation;
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
    private EditText etIeltsScore;

    // --- STEP 3 INPUTS (Interests) ---
    private TextView tvMultiCountries, tvMultiFields;
    private EditText etBudget;

    // Multi-Select Data
    private ArrayList<Integer> selectedCountryIndices = new ArrayList<>();
    private ArrayList<Integer> selectedFieldIndices = new ArrayList<>();

    // Arrays
    private final String[] countriesWithFlags = {"🇵🇰 Pakistan", "🇮🇳 India", "🇺🇸 USA", "🇬🇧 UK", "🇨🇦 Canada", "🇦🇺 Australia", "🇩🇪 Germany", "🇨🇳 China", "🇦🇪 UAE"};
    private final String[] qualifications = {"Select Qualification", "Matric / O-Levels", "Intermediate / A-Levels", "Bachelor's (BS)", "Master's (MS)", "PhD"};
    private final String[] targetCountriesList = {"USA", "UK", "Canada", "Australia", "Germany", "Italy", "France", "Sweden", "Turkey", "China", "Malaysia"};
    private final String[] studyFieldsList = {"Computer Science", "Software Engineering", "Business", "Medicine", "Engineering", "Arts & Design", "Law", "Data Science"};

    // Firebase & Location
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorage;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference(Constants.DB_PROFILE_IMAGES);

        // Init Location Client
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔥 1. INITIALIZE ALL VIEWS FIRST (Most Important)
        initViews();

        // 2. Setup Spinners
        setupSpinners();

        // 3. Update UI for Step 1
        updateUI();

        // 4. Load Existing Data
        loadUserData();

        // 5. Auto Location
        getLocationAndFill();

        // --- CLICK LISTENERS ---

        if (btnAutoLocation != null) {
            btnAutoLocation.setOnClickListener(v -> getLocationAndFill());
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (currentStep == 1 && validateStep1()) {
                    currentStep = 2;
                    updateUI();
                } else if (currentStep == 2 && validateStep2()) {
                    currentStep = 3;
                    updateUI();
                } else if (currentStep == 3 && validateStep3()) {
                    saveDataToFirebase();
                }
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (currentStep > 1) {
                    currentStep--;
                    updateUI();
                } else {
                    finish();
                }
            });
        }

        if (ivProfileUpload != null) {
            ivProfileUpload.setOnClickListener(v -> pickImageFromGallery());
        }

        if (tvMultiCountries != null) {
            tvMultiCountries.setOnClickListener(v -> showMultiSelectDialog("Select Target Countries", targetCountriesList, selectedCountryIndices, tvMultiCountries));
        }
        if (tvMultiFields != null) {
            tvMultiFields.setOnClickListener(v -> showMultiSelectDialog("Select Study Fields", studyFieldsList, selectedFieldIndices, tvMultiFields));
        }
    }

    // --- LOCATION LOGIC ---
    private void getLocationAndFill() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        if (fusedLocationClient == null) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(EditProfileActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                if (addresses != null && !addresses.isEmpty()) {
                                    String city = addresses.get(0).getLocality();
                                    String country = addresses.get(0).getCountryName();

                                    // Crash Fix: Check null before setText
                                    if (etCity != null && city != null) etCity.setText(city);
                                    if (spinnerCountry != null && country != null) selectSpinnerValue(spinnerCountry, country);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void selectSpinnerValue(Spinner spinner, String geocoderCountry) {
        if (spinner == null || geocoderCountry == null) return;

        if (geocoderCountry.equalsIgnoreCase("United States")) geocoderCountry = "USA";
        if (geocoderCountry.equalsIgnoreCase("United Kingdom")) geocoderCountry = "UK";
        if (geocoderCountry.equalsIgnoreCase("United Arab Emirates")) geocoderCountry = "UAE";

        for (int i = 0; i < spinner.getCount(); i++) {
            String spinnerItem = spinner.getItemAtPosition(i).toString().toLowerCase();
            if (spinnerItem.contains(geocoderCountry.toLowerCase())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndFill();
        }
    }

    // --- INITIALIZATION ---
    private void initViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        iconStep1 = findViewById(R.id.iconStep1); txtStep1 = findViewById(R.id.txtStep1);
        iconStep2 = findViewById(R.id.iconStep2); txtStep2 = findViewById(R.id.txtStep2);
        iconStep3 = findViewById(R.id.iconStep3); txtStep3 = findViewById(R.id.txtStep3);
        viewLine1 = findViewById(R.id.viewLine1);
        viewLine2 = findViewById(R.id.viewLine2);

        if (iconStep1 != null) cvStep1 = (CardView) iconStep1.getParent();
        cvStep2 = findViewById(R.id.cvStep2);
        cvStep3 = findViewById(R.id.cvStep3);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        ivProfileUpload = findViewById(R.id.ivProfileUpload);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnAutoLocation = findViewById(R.id.btnAutoLocation);

        spinnerHighestQual = findViewById(R.id.spinnerHighestQual);
        cvMasters = findViewById(R.id.cvMastersDetails);
        cvBachelors = findViewById(R.id.cvBachelorsDetails);
        cvInter = findViewById(R.id.cvInterDetails);
        cvMatric = findViewById(R.id.cvMatricDetails);

        etMastersUni = findViewById(R.id.etMastersUni); etMastersCgpa = findViewById(R.id.etMastersCgpa);
        etBachelorsUni = findViewById(R.id.etBachelorsUni); etBachelorsCgpa = findViewById(R.id.etBachelorsCgpa);
        etInterInstitute = findViewById(R.id.etInterInstitute); etInterMarks = findViewById(R.id.etInterMarks);
        etMatricInstitute = findViewById(R.id.etMatricInstitute); etMatricMarks = findViewById(R.id.etMatricMarks);

        etIeltsScore = findViewById(R.id.etIeltsScore);
        tvMultiCountries = findViewById(R.id.tvMultiCountries);
        tvMultiFields = findViewById(R.id.tvMultiFields);
        etBudget = findViewById(R.id.etBudget);
    }

    private void setupSpinners() {
        if (spinnerCountry == null || spinnerHighestQual == null) return;

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countriesWithFlags);
        spinnerCountry.setAdapter(countryAdapter);

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

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(document -> {
                    // 🔥 SAFETY CHECK: Agar activity band ho chuki hai to kuch mat karo
                    if (isFinishing() || isDestroyed()) return;

                    if (document.exists()) {
                        try {
                            // Safe Setting with NULL Checks
                            if (etFirstName != null && document.contains(Constants.KEY_FIRST_NAME))
                                etFirstName.setText(document.getString(Constants.KEY_FIRST_NAME));

                            if (etLastName != null && document.contains(Constants.KEY_LAST_NAME))
                                etLastName.setText(document.getString(Constants.KEY_LAST_NAME));

                            if (etPhone != null && document.contains(Constants.KEY_PHONE))
                                etPhone.setText(document.getString(Constants.KEY_PHONE));

                            if (etCity != null && document.contains(Constants.KEY_CITY))
                                etCity.setText(document.getString(Constants.KEY_CITY));

                            if (spinnerCountry != null && document.contains(Constants.KEY_COUNTRY))
                                selectSpinnerValue(spinnerCountry, document.getString(Constants.KEY_COUNTRY));

                            String profileImage = document.getString(Constants.KEY_PROFILE_IMAGE);
                            if (profileImage != null && !profileImage.isEmpty() && ivProfileUpload != null) {
                                Glide.with(EditProfileActivity.this).load(profileImage).placeholder(R.drawable.user_profile).into(ivProfileUpload);
                            }

                            // Interests
                            if (tvMultiCountries != null && document.contains(Constants.KEY_TARGET_COUNTRIES)) {
                                String targetCountries = document.getString(Constants.KEY_TARGET_COUNTRIES);
                                tvMultiCountries.setText(targetCountries);
                                updateIndicesFromText(targetCountries, targetCountriesList, selectedCountryIndices);
                            }

                            if (tvMultiFields != null && document.contains(Constants.KEY_INTERESTED_FIELDS)) {
                                String studyFields = document.getString(Constants.KEY_INTERESTED_FIELDS);
                                tvMultiFields.setText(studyFields);
                                updateIndicesFromText(studyFields, studyFieldsList, selectedFieldIndices);
                            }

                            if (etBudget != null && document.contains(Constants.KEY_BUDGET)) {
                                Double budget = document.getDouble(Constants.KEY_BUDGET);
                                if (budget != null) etBudget.setText(String.valueOf(budget.intValue()));
                            }

                            // Education
                            if (document.contains(Constants.KEY_QUALIFICATION) && spinnerHighestQual != null) {
                                String qual = document.getString(Constants.KEY_QUALIFICATION);
                                selectSpinnerValue(spinnerHighestQual, qual);
                                updateEducationVisibility(qual);

                                if (qual != null) {
                                    if (qual.contains("Master")) {
                                        if(etMastersUni != null) etMastersUni.setText(document.getString(Constants.KEY_UNIVERSITY_NAME));
                                        if(etMastersCgpa != null) etMastersCgpa.setText(document.getString(Constants.KEY_LAST_GRADES));
                                    } else if (qual.contains("Bachelor")) {
                                        if(etBachelorsUni != null) etBachelorsUni.setText(document.getString(Constants.KEY_UNIVERSITY_NAME));
                                        if(etBachelorsCgpa != null) etBachelorsCgpa.setText(document.getString(Constants.KEY_LAST_GRADES));
                                    } else if (qual.contains("Inter")) {
                                        if(etInterInstitute != null) etInterInstitute.setText(document.getString(Constants.KEY_COLLEGE_NAME));
                                        if(etInterMarks != null) etInterMarks.setText(document.getString(Constants.KEY_LAST_GRADES));
                                    }
                                }
                            }

                            if (etIeltsScore != null && document.contains(Constants.KEY_IELTS_SCORE)) {
                                Double ielts = document.getDouble(Constants.KEY_IELTS_SCORE);
                                if (ielts != null) etIeltsScore.setText(String.valueOf(ielts));
                            }
                        } catch (Exception e) {
                            LogUtils.e("EditProfile", "Error loading data: " + e.getMessage());
                        }
                    }
                });
    }

    private void updateIndicesFromText(String text, String[] masterList, ArrayList<Integer> indicesList) {
        if (text == null) return;
        indicesList.clear();
        String[] parts = text.split(", ");
        for (String part : parts) {
            for (int i = 0; i < masterList.length; i++) {
                if (masterList[i].equalsIgnoreCase(part.trim())) {
                    indicesList.add(i);
                    break;
                }
            }
        }
    }

    private void updateUI() {
        if (layoutStep1 == null) return; // Basic check

        layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        if (btnBack != null) btnBack.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        if (btnNext != null) btnNext.setText(currentStep == 3 ? "Save Profile" : "Next Step");

        int activeColor = ContextCompat.getColor(this, R.color.brand_secondary);

        if (cvStep2 != null) resetSidebarStyle(cvStep2, iconStep2, txtStep2);
        if (cvStep3 != null) resetSidebarStyle(cvStep3, iconStep3, txtStep3);

        if (viewLine1 != null) viewLine1.setBackgroundColor(Color.parseColor("#CCCCCC"));
        if (viewLine2 != null) viewLine2.setBackgroundColor(Color.parseColor("#CCCCCC"));

        if (currentStep >= 2 && cvStep2 != null) { setSidebarActive(cvStep2, iconStep2, txtStep2); viewLine1.setBackgroundColor(activeColor); }
        if (currentStep >= 3 && cvStep3 != null) { setSidebarActive(cvStep3, iconStep3, txtStep3); viewLine2.setBackgroundColor(activeColor); }
    }

    private void setSidebarActive(CardView card, ImageView icon, TextView text) {
        if (card == null) return;
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.brand_secondary));
        if (icon != null) icon.setColorFilter(Color.WHITE);
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(this, R.color.brand_secondary));
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetSidebarStyle(CardView card, ImageView icon, TextView text) {
        if (card == null) return;
        card.setCardBackgroundColor(Color.WHITE);
        if (icon != null) icon.setColorFilter(Color.parseColor("#AAAAAA"));
        if (text != null) {
            text.setTextColor(Color.parseColor("#AAAAAA"));
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void updateEducationVisibility(String qual) {
        if(cvMasters != null) cvMasters.setVisibility(View.GONE);
        if(cvBachelors != null) cvBachelors.setVisibility(View.GONE);
        if(cvInter != null) cvInter.setVisibility(View.GONE);
        if(cvMatric != null) cvMatric.setVisibility(View.GONE);

        if (qual == null) return;

        if (qual.contains("Master")) {
            if(cvMasters != null) cvMasters.setVisibility(View.VISIBLE);
            if(cvBachelors != null) cvBachelors.setVisibility(View.VISIBLE);
            if(cvInter != null) cvInter.setVisibility(View.VISIBLE);
            if(cvMatric != null) cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Bachelor")) {
            if(cvBachelors != null) cvBachelors.setVisibility(View.VISIBLE);
            if(cvInter != null) cvInter.setVisibility(View.VISIBLE);
            if(cvMatric != null) cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Inter")) {
            if(cvInter != null) cvInter.setVisibility(View.VISIBLE);
            if(cvMatric != null) cvMatric.setVisibility(View.VISIBLE);
        } else if (qual.contains("Matric")) {
            if(cvMatric != null) cvMatric.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateStep1() {
        if (etFirstName == null) return false;
        String firstName = ValidationUtils.trimString(etFirstName.getText().toString());
        if (!ValidationUtils.isValidName(firstName)) { etFirstName.setError("First name is required"); return false; }
        return true;
    }
    private boolean validateStep2() {
        if (spinnerHighestQual == null || spinnerHighestQual.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select Qualification", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }
    private boolean validateStep3() {
        if (tvMultiCountries == null || tvMultiCountries.getText().toString().contains("Tap to select")) {
            Toast.makeText(this, "Select at least 1 country", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (ivProfileUpload != null) ivProfileUpload.setImageURI(imageUri);
                }
            }
    );

    private void pickImageFromGallery() {
        Intent intent = new Intent(); intent.setType("image/*"); intent.setAction(Intent.ACTION_GET_CONTENT); imagePickerLauncher.launch(intent);
    }

    private void showMultiSelectDialog(String title, String[] items, ArrayList<Integer> selectedItems, TextView targetView) {
        boolean[] checkedItems = new boolean[items.length];
        for (int i : selectedItems) checkedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) selectedItems.add(which); else selectedItems.remove(Integer.valueOf(which));
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder sb = new StringBuilder();
            Collections.sort(selectedItems);
            for (int i = 0; i < selectedItems.size(); i++) {
                sb.append(items[selectedItems.get(i)]);
                if (i != selectedItems.size() - 1) sb.append(", ");
            }
            if (targetView != null) targetView.setText(sb.toString());
        });
        builder.show();
    }

    private void saveDataToFirebase() {
        Toast.makeText(this, "Saving Profile...", Toast.LENGTH_SHORT).show();
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        if (imageUri != null) {
            StorageReference fileRef = mStorage.child(uid + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveDetailsToDatabase(uid, uri.toString())));
        } else {
            saveDetailsToDatabase(uid, null);
        }
    }

    private void saveDetailsToDatabase(String uid, String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        String firstName = etFirstName != null ? ValidationUtils.trimString(etFirstName.getText().toString()) : "";
        String lastName = etLastName != null ? ValidationUtils.trimString(etLastName.getText().toString()) : "";

        map.put(Constants.KEY_FULL_NAME, firstName + " " + lastName);
        map.put(Constants.KEY_FIRST_NAME, firstName);
        map.put(Constants.KEY_LAST_NAME, lastName);
        if(etPhone != null) map.put(Constants.KEY_PHONE, ValidationUtils.trimString(etPhone.getText().toString()));
        if(etCity != null) map.put(Constants.KEY_CITY, ValidationUtils.trimString(etCity.getText().toString()));
        if(spinnerCountry != null) map.put(Constants.KEY_COUNTRY, spinnerCountry.getSelectedItem().toString());
        if (imageUrl != null) map.put(Constants.KEY_PROFILE_IMAGE, imageUrl);

        if(tvMultiCountries != null) map.put(Constants.KEY_TARGET_COUNTRIES, tvMultiCountries.getText().toString());
        if(tvMultiFields != null) map.put(Constants.KEY_INTERESTED_FIELDS, tvMultiFields.getText().toString());

        String qual = "";
        if(spinnerHighestQual != null) {
            qual = spinnerHighestQual.getSelectedItem().toString();
            map.put(Constants.KEY_QUALIFICATION, qual);
        }

        if (qual.contains("Master")) {
            if(etMastersUni != null) map.put(Constants.KEY_UNIVERSITY_NAME, etMastersUni.getText().toString());
            if(etMastersCgpa != null) map.put(Constants.KEY_LAST_GRADES, etMastersCgpa.getText().toString());
            try { map.put(Constants.KEY_CGPA, Double.parseDouble(etMastersCgpa.getText().toString())); } catch (Exception e) {}
        } else if (qual.contains("Bachelor")) {
            if(etBachelorsUni != null) map.put(Constants.KEY_UNIVERSITY_NAME, etBachelorsUni.getText().toString());
            if(etBachelorsCgpa != null) map.put(Constants.KEY_LAST_GRADES, etBachelorsCgpa.getText().toString());
            try { map.put(Constants.KEY_CGPA, Double.parseDouble(etBachelorsCgpa.getText().toString())); } catch (Exception e) {}
        }

        if (etIeltsScore != null) {
            String ielts = etIeltsScore.getText().toString();
            if (!ielts.isEmpty()) map.put(Constants.KEY_IELTS_SCORE, Double.parseDouble(ielts));
        }

        if (etBudget != null) {
            String budget = etBudget.getText().toString();
            if (!budget.isEmpty()) map.put(Constants.KEY_BUDGET, Double.parseDouble(budget));
        }

        db.collection("Users").document(uid).set(map, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                    finish();
                });
    }
}