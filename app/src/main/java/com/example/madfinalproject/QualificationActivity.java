package com.example.madfinalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QualificationActivity extends AppCompatActivity {

    private TextView tvSelectedQualification, tvRequirementsLabel;
    private LinearLayout btnSelectQualification, llDocumentsContainer;
    private View btnSaveDocs;

    // Qualifications ki List
    private final String[] qualifications = {"Intermediate / A-Levels", "Bachelor's Degree (BS)", "Master's Degree (MS)", "PhD"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qualification);

        // Initialize Views
        tvSelectedQualification = findViewById(R.id.tvSelectedQualification);
        btnSelectQualification = findViewById(R.id.btnSelectQualification);
        llDocumentsContainer = findViewById(R.id.llDocumentsContainer);
        tvRequirementsLabel = findViewById(R.id.tvRequirementsLabel);
        btnSaveDocs = findViewById(R.id.btnSaveDocs);

        // Click Listener on Qualification Dropdown
        btnSelectQualification.setOnClickListener(v -> showQualificationDialog());

        // Save Button Listener
        btnSaveDocs.setOnClickListener(v -> {
            Toast.makeText(this, "Documents Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 1. Qualification select karne ka Dialog
    private void showQualificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Qualification");
        builder.setItems(qualifications, (dialog, which) -> {
            String selected = qualifications[which];
            tvSelectedQualification.setText(selected);

            // Selection ke mutabiq Documents show karein
            generateDocumentList(selected);
        });
        builder.show();
    }

    // 2. Logic: Qualification ke hisaab se list banao
    private void generateDocumentList(String qualification) {
        // Container ko khali karein taake purani items hat jayen
        llDocumentsContainer.removeAllViews();

        // UI Visible karein
        tvRequirementsLabel.setVisibility(View.VISIBLE);
        btnSaveDocs.setVisibility(View.VISIBLE);

        List<String> requiredDocs = new ArrayList<>();

        // Logic check karein
        if (qualification.equals("Intermediate / A-Levels")) {
            requiredDocs.add("Matric / O-Levels Certificate");
            requiredDocs.add("Intermediate / A-Levels Result");
            requiredDocs.add("English Proficiency (IELTS/TOEFL) - Optional");
        }
        else if (qualification.equals("Bachelor's Degree (BS)")) {
            requiredDocs.add("Matric / O-Levels Certificate");
            requiredDocs.add("Intermediate / A-Levels Result");
            requiredDocs.add("Bachelor's Transcript (Official)");
            requiredDocs.add("Bachelor's Degree Certificate");
            requiredDocs.add("English Proficiency Certificate");
        }
        else if (qualification.equals("Master's Degree (MS)")) {
            requiredDocs.add("Bachelor's Degree & Transcript");
            requiredDocs.add("Master's Degree & Transcript");
            requiredDocs.add("Research Proposal");
            requiredDocs.add("English Proficiency Certificate");
        }
        else {
            // Default
            requiredDocs.add("Identity Document (CNIC/Passport)");
            requiredDocs.add("Previous Academic Records");
        }

        // 3. Har document ke liye View banayein aur screen par lagayein
        for (String docName : requiredDocs) {
            addDocumentView(docName);
        }
    }

    // 4. Dynamic View Creator
    private void addDocumentView(String docTitle) {
        // 'item_document_upload.xml' ko inflate (convert) karein
        View view = LayoutInflater.from(this).inflate(R.layout.item_document_upload, llDocumentsContainer, false);

        // View ke andar items dhoondein
        TextView tvTitle = view.findViewById(R.id.tvDocTitle);
        LinearLayout llClick = view.findViewById(R.id.llUploadClick);
        ImageView ivStatus = view.findViewById(R.id.ivStatusCheck);

        // Naam set karein
        tvTitle.setText(docTitle);

        // Click Listener (Upload ke liye)
        llClick.setOnClickListener(v -> {
            // Yahan File Picker khulega (Next Step)
            Toast.makeText(this, "Upload: " + docTitle, Toast.LENGTH_SHORT).show();

            // Testing ke liye tick mark show kar rahe hain
            ivStatus.setVisibility(View.VISIBLE);
        });

        // Main Container mein add karein
        llDocumentsContainer.addView(view);
    }
}