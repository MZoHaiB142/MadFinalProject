package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.example.madfinalproject.utils.ValidationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPasswordActivity extends AppCompatActivity {

    // Variables declare karein
    private EditText etResetEmail;
    private Button btnResetPassword;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword); // XML file ka naam check karein

        // 1. UI Views Initialize karein (XML wali IDs use karein)
        etResetEmail = findViewById(R.id.etResetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // 2. Firebase Initialize karein
        mAuth = FirebaseAuth.getInstance();

        // 3. Reset Button par click listener lagayein
        btnResetPassword.setOnClickListener(v -> {
            resetUserPassword();
        });

        // 4. UI Design fix (Padding wagera, jo aapki baaki apps mein hai)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void resetUserPassword() {
        String email = ValidationUtils.trimString(etResetEmail.getText().toString());

        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            etResetEmail.setError(Constants.ERROR_EMAIL_REQUIRED);
            etResetEmail.requestFocus();
            return;
        }

        LogUtils.d("ForgotPasswordActivity", "Sending password reset email to: " + email);

        // Send password reset email via Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LogUtils.d("ForgotPasswordActivity", "Password reset email sent successfully");
                        Toast.makeText(forgotPasswordActivity.this,
                                Constants.SUCCESS_PASSWORD_RESET_SENT,
                                Toast.LENGTH_LONG).show();
                        finish(); // Return to login screen
                    } else {
                        String errorMessage = Constants.ERROR_UNKNOWN;
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            LogUtils.e("ForgotPasswordActivity", "Password reset failed", task.getException());
                        }
                        Toast.makeText(forgotPasswordActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}