package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Google Imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

// Facebook Imports
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FacebookAuthProvider;

import java.util.Arrays;

public class loginActivity extends AppCompatActivity {

    // Firebase & UI Variables
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button btnContinue;

    // Google Variables
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // Facebook Variables
    private CallbackManager mCallbackManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ STEP 1: Facebook Init sabse pehle (Crash se bachne ke liye)
        com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());

        // ✅ STEP 2: Layout set karein (SIRF EK BAAR)
        setContentView(R.layout.login);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // 2. Initialize Views
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        btnContinue = findViewById(R.id.btncontinue);

        // Navigation Buttons
        Button btnTabSignup = findViewById(R.id.btnTabSignup);
        TextView tvForgotPassword = findViewById(R.id.forgotP);
        TextView tvSignupLink = findViewById(R.id.tvSignupLink);

        // UI Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ⚠️ DHYAAN DEIN: Maine yahan se doosra 'setContentView' HATA DIYA hai.

        // ==========================================
        // GOOGLE SETUP
        // ==========================================
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("744209892194-6k91hb0n24moamqap6acuhahv0kie1v4.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInWithGoogle());

        // ==========================================
        // FACEBOOK SETUP
        // ==========================================
        mCallbackManager = CallbackManager.Factory.create();

        findViewById(R.id.btnFacebook).setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(loginActivity.this, Arrays.asList("email", "public_profile"));
        });

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(loginActivity.this, "Facebook Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(loginActivity.this, "Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // ==========================================
        // LISTENERS
        // ==========================================
        btnContinue.setOnClickListener(v -> loginUser());

        btnTabSignup.setOnClickListener(v -> goToSignup());
        tvSignupLink.setOnClickListener(v -> goToSignup());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(loginActivity.this, forgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void goToSignup() {
        Intent intent = new Intent(loginActivity.this, signupActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        // Debug Toast
        Toast.makeText(this, "Attempting Login...", Toast.LENGTH_SHORT).show();

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        Toast.makeText(loginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        Toast.makeText(loginActivity.this, "Google Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        Toast.makeText(loginActivity.this, "Facebook Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard() {
        Toast.makeText(loginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(loginActivity.this, dashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}