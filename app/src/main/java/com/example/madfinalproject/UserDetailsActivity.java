package com.example.madfinalproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.madfinalproject.api.SopApiRequest;
import com.example.madfinalproject.models.SopRequest;
import com.example.madfinalproject.models.SopRules;
import com.example.madfinalproject.repository.SopRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    // ── Views ──
    private EditText     etTargetProgram, etTargetCountry;
    private EditText     etMainProject, etWorkDetails;
    private EditText     etSponsorRelation, etSponsorProfession;
    private EditText     etCareerGoals;
    private SwitchCompat switchWorkExp;
    private Button       btnGenerate;

    // Tags
    private TextView tagSoftware, tagFintech, tagAi, tagTelecom;
    private String   selectedIndustry = "Software Houses"; // Default
    private TextInputEditText etPreviousEducation;
    private TextInputEditText etKeySubjects;

    // ── Data ──
    private String countryName   = "";
    private String universityName = "";

    // ── Repository ──
    private SopRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sop_user_detail);

        initViews();
        setupWorkExperienceSwitch();
        setupTags();

        // Previous screens se data
        countryName    = getIntent().getStringExtra("COUNTRY_NAME");
        universityName = getIntent().getStringExtra("UNIVERSITY_NAME");

        // Country pre-fill karo
        if (countryName != null && !countryName.isEmpty()) {
            etTargetCountry.setText(countryName);
        }

        repository = new SopRepository();

        btnGenerate.setOnClickListener(v -> {
            if (validateForm()) {
                startSopGeneration();
            }
        });
    }

    // ─────────────────────────────────────────────
    // INIT VIEWS
    // ─────────────────────────────────────────────
    private void initViews() {
        etTargetProgram     = findViewById(R.id.et_target_program);
        etTargetCountry     = findViewById(R.id.et_target_country);
        etMainProject       = findViewById(R.id.et_main_project);
        switchWorkExp       = findViewById(R.id.switch_work_exp);
        etWorkDetails       = findViewById(R.id.et_work_details);
        etSponsorRelation   = findViewById(R.id.et_sponsor_relation);
        etPreviousEducation = findViewById(R.id.etPreviousEducation);
        etSponsorProfession = findViewById(R.id.et_sponsor_profession);
        etCareerGoals       = findViewById(R.id.et_career_goals);
        btnGenerate         = findViewById(R.id.btn_generate);
        etKeySubjects = findViewById(R.id.etKeySubjects);

        tagSoftware = findViewById(R.id.tag_software);
        tagFintech  = findViewById(R.id.tag_fintech);
        tagAi       = findViewById(R.id.tag_ai);
        tagTelecom  = findViewById(R.id.tag_telecom);
    }

    // ─────────────────────────────────────────────
    // WORK EXPERIENCE SWITCH LOGIC
    // ─────────────────────────────────────────────
    private void setupWorkExperienceSwitch() {
        switchWorkExp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etWorkDetails.setVisibility(View.VISIBLE);
            } else {
                etWorkDetails.setVisibility(View.GONE);
                etWorkDetails.setText(""); // Clear text if turned off
            }
        });
    }

    // ─────────────────────────────────────────────
    // TARGET INDUSTRY TAGS LOGIC
    // ─────────────────────────────────────────────
    private void setupTags() {
        tagSoftware.setOnClickListener(v -> selectTag(tagSoftware, "Software Houses"));
        tagFintech.setOnClickListener(v -> selectTag(tagFintech, "FinTech"));
        tagAi.setOnClickListener(v -> selectTag(tagAi, "AI Startups"));
        tagTelecom.setOnClickListener(v -> selectTag(tagTelecom, "Telecommunications"));

        // Default selection
        selectTag(tagSoftware, "Software Houses");
    }

    private void selectTag(TextView selectedView, String industryName) {
        selectedIndustry = industryName;

        // Reset sab tags ko unselected color do
        resetTagUI(tagSoftware);
        resetTagUI(tagFintech);
        resetTagUI(tagAi);
        resetTagUI(tagTelecom);

        // Selected wale ko highlight karo (Blue background, White text)
        selectedView.setBackgroundResource(R.drawable.bg_chip_active_blue);
        selectedView.setTextColor(Color.WHITE);
    }

    private void resetTagUI(TextView tag) {
        // Aapke XML wala unselected background
        tag.setBackgroundResource(R.drawable.bg_tag_unselected);
        tag.setTextColor(Color.parseColor("#4B5563"));
    }

    // ─────────────────────────────────────────────
    // MAIN FLOW: SOP GENERATION
    // ─────────────────────────────────────────────
    private void startSopGeneration() {
        String country = etTargetCountry.getText().toString().trim();

        setLoading(true);

        repository.getSopRules(country, new SopRepository.RulesCallback() {

            @Override
            public void onSuccess(SopRules rules) {

                String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "anonymous";

                // 🔥 Data mapping for the new AI requirements
                // 🔥 Data extraction
                String program   = etTargetProgram.getText().toString().trim();
                String project   = etMainProject.getText().toString().trim();
                String subjectsStr = etKeySubjects.getText().toString().trim();
                String education = etPreviousEducation.getText().toString().trim();
                if(education.isEmpty()) {
                    education = "Relevant Bachelor's Degree";
                }

                String experience = switchWorkExp.isChecked()
                        ? etWorkDetails.getText().toString().trim()
                        : "No formal work experience.";

                String sponsorRel  = etSponsorRelation.getText().toString().trim();
                String sponsorProf = etSponsorProfession.getText().toString().trim();
                String goals       = etCareerGoals.getText().toString().trim();

                // 🔥 Naye 14 parameters ke sath SopRequest create karo
                SopRequest sopRequest = new SopRequest(
                        userId,
                        country,
                        universityName != null ? universityName : "",
                        program,
                        subjectsStr,
                        education,       // 🔥 Ab yahan actual user ki education jayegi
                        "",              // cgpa abhi bhi form mein nahi hai (khali rehne dein)
                        project,
                        experience,
                        goals,
                        selectedIndustry,
                        "Sponsored",
                        sponsorRel,
                        sponsorProf,
                        "No"
                );

                repository.saveSopRequest(sopRequest, new SopRepository.SopSavedCallback() {
                    @Override
                    public void onSuccess(String requestId) {

                        SopApiRequest apiRequest = buildApiRequest(userId, requestId, sopRequest, rules);

                        repository.generateSopFromApi(apiRequest, requestId, userId,
                                new SopRepository.SopGeneratedCallback() {
                                    @Override
                                    public void onSuccess(com.example.madfinalproject.models.GeneratedSop sop) {
                                        runOnUiThread(() -> {
                                            setLoading(false);
                                            goToResultScreen(sop, requestId);
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(() -> {
                                            setLoading(false);
                                            showError("SOP generation failed: " + error);
                                        });
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Request save failed: " + error);
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError("Rules fetch failed: " + error);
                });
            }
        });
    }

    // ─────────────────────────────────────────────
    // API Request build
    // ─────────────────────────────────────────────
    private SopApiRequest buildApiRequest(String userId, String requestId, SopRequest req, SopRules rules) {
        SopApiRequest api = new SopApiRequest();

        // Basic Info
        api.setUserId(userId);
        api.setRequestId(requestId);
        api.setCountry(req.getCountry());
        api.setUniversity(req.getUniversity());
        api.setCourse(etTargetProgram.getText().toString().trim());

        // Academic Info (Bhejna alag alag hai ab!)
        api.setEducation(req.getEducation()); // Agar form mein separate education input hai
        api.setFinalYearProject(etMainProject.getText().toString().trim());
        // Agar CGPA ka edit text hai toh: api.setCgpa(etGpa.getText().toString().trim());
        api.setStudyGap("No");

        // Experience
        if(switchWorkExp.isChecked()) {
            api.setExperience(etWorkDetails.getText().toString().trim());
        } else {
            api.setExperience("None");
        }
        if (!req.getKeySubjects().isEmpty()) {
            List<String> subjectsList = Arrays.asList(req.getKeySubjects().split("\\s*,\\s*"));
            api.setUniversityCurriculum(subjectsList);
        }

        // Future Goals & Return Intent
        api.setFutureGoals(etCareerGoals.getText().toString().trim());
        api.setTargetIndustry(selectedIndustry);

        // Financials
        api.setSponsorRelationship(etSponsorRelation.getText().toString().trim());
        api.setSponsorProfession(etSponsorProfession.getText().toString().trim());

        // Rules Mapping
        api.setWordCountRules(rules.getWordCountRules());
        api.setPersonalizationRequirements(rules.getPersonalizationRequirements());
        api.setPersonalMotivationSection(rules.getPersonalMotivationSection());
        api.setAntiAiPatterns(rules.getAntiAiPatterns());
        api.setWhyUniversityEnhancement(rules.getWhyUniversityEnhancement());
        api.setWhyNotHomeCountry(rules.getWhyNotHomeCountry());
        api.setReturnIntentStrengthening(rules.getReturnIntentStrengthening());
        api.setGenuineStudentRequirements(rules.getGenuineStudentRequirements());
        api.setQualityChecks(rules.getQualityChecks());
        api.setHumanizationRules(rules.getHumanizationRules());
        api.setProfessionalExperienceRules(rules.getProfessionalExperienceRules());
        api.setCourseJustificationRules(rules.getCourseJustificationRules());
        api.setCareerGoalRules(rules.getCareerGoalRules());
        api.setFinancialViabilityEnhancement(rules.getFinancialViabilityEnhancement());
        api.setSopFinalScoring(rules.getSopFinalScoring());

        return api;
    }
      // ─────────────────────────────────────────────
    // Result screen navigation
    // ─────────────────────────────────────────────
    private void goToResultScreen(com.example.madfinalproject.models.GeneratedSop sop, String requestId) {
        Intent intent = new Intent(this, SopResultActivity.class);
        intent.putExtra("SOP_TEXT",        sop.getSopText());
        intent.putExtra("SOP_SCORE",       sop.getScore());
        intent.putExtra("WORD_COUNT",      sop.getWordCount());
        intent.putExtra("COUNTRY_NAME",    sop.getCountry());
        intent.putExtra("UNIVERSITY_NAME", universityName);
        intent.putExtra("REQUEST_ID",      requestId);
        intent.putExtra("MOTIVATION_SCORE",    sop.getMotivationScore());
        intent.putExtra("VISA_STRENGTH",       sop.getVisaStrengthScore());
        intent.putExtra("CLARITY_SCORE",       sop.getClarityScore());
        intent.putExtra("CAREER_ALIGN_SCORE",  sop.getCareerAlignmentScore());
        startActivity(intent);
    }

    // ── Validation ──
    private boolean validateForm() {
        if (etTargetProgram.getText().toString().trim().isEmpty()) {
            etTargetProgram.setError("Program likhein");
            return false;
        }
        if (etMainProject.getText().toString().trim().isEmpty()) {
            etMainProject.setError("Project/FYP details are required");
            return false;
        }
        if (etTargetCountry.getText().toString().trim().isEmpty()) {
            etTargetCountry.setError("Country likhein");
            return false;
        }
        if (etSponsorRelation.getText().toString().trim().isEmpty()) {
            etSponsorRelation.setError("Sponsor ka relation likhein");
            return false;
        }
        if (etCareerGoals.getText().toString().trim().isEmpty()) {
            etCareerGoals.setError("Career goals likhein");
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        btnGenerate.setEnabled(!loading);
        btnGenerate.setText(loading ? "Generating ✦..." : "Generate SOP ✦");

        // Agar aapke paas actual progress bar hai XML mein toh usay hide/show karein
        // progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
