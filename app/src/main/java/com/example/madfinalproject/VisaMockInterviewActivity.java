package com.example.madfinalproject;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.madfinalproject.utils.Constants;
import androidx.appcompat.app.AppCompatActivity;

public class VisaMockInterviewActivity extends AppCompatActivity {
    private RadioGroup countryGroup;
    private Button startButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_interview_preparation);

        countryGroup = findViewById(R.id.countryGroup);
        startButton = findViewById(R.id.btnStartInterview);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        countryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            startButton.setEnabled(checkedId != -1);
            startButton.setAlpha(checkedId != -1 ? 1f : 0.55f);
        });

        startButton.setOnClickListener(v -> {
            if (countryGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedCountry = selectedCountry(countryGroup.getCheckedRadioButtonId());
            startActivity(new Intent(this, VisaInterviewDashboardActivity.class)
                    .putExtra(Constants.EXTRA_INTERVIEW_COUNTRY, selectedCountry));
        });
    }

    private String selectedCountry(int checkedId) {
        if (checkedId == R.id.countryUsa) return "United States";
        if (checkedId == R.id.countryAustralia) return "Australia";
        if (checkedId == R.id.countryCanada) return "Canada";
        if (checkedId == R.id.countryUk) return "United Kingdom";
        return "Germany";
    }
}
