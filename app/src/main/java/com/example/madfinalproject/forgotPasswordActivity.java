package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        Button btnResetPassword = findViewById(R.id.btnResetPassword);

    }

    private void resetUserPassword() {
        String email = etResetEmail.getText().toString().trim();

        // Check karein ke Email field khali na ho
        if (TextUtils.isEmpty(email)) {
            etResetEmail.setError("Email is required");
            etResetEmail.requestFocus();
            return;
        }

        // Firebase ko bolein ke Link Bheje
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Agar email chala gaya
                            Toast.makeText(forgotPasswordActivity.this,
                                    "Reset link sent to your email. Please check your inbox.",
                                    Toast.LENGTH_LONG).show();

                            // User ko wapas login screen par bhej dein (Optional)
                            finish();
                        } else {
                            // Agar koi masla hua (User nahi mila, ya internet issue)
                            String error = task.getException().getMessage();
                            Toast.makeText(forgotPasswordActivity.this,
                                    "Error: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}