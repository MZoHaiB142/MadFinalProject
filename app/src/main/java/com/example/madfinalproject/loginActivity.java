package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.example.madfinalproject.utils.ValidationUtils;

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

    // Facebook Variables
    private CallbackManager mCallbackManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());


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



        // ==========================================
        // GOOGLE SETUP
        // ==========================================
        // TODO: Move Google OAuth Client ID to strings.xml or build config for security
        String googleClientId = getString(R.string.google_oauth_client_id);
        if (TextUtils.isEmpty(googleClientId)) {
            // Fallback for development (should be removed in production)
            googleClientId = "744209892194-6k91hb0n24moamqap6acuhahv0kie1v4.apps.googleusercontent.com";
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
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

    }
    private void goToSignup() {
        Intent intent = new Intent(loginActivity.this, signupActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        String email = ValidationUtils.trimString(emailInput.getText().toString());
        String password = ValidationUtils.trimString(passwordInput.getText().toString());

        // Validation
        if (!ValidationUtils.isValidEmail(email)) {
            emailInput.setError(Constants.ERROR_EMAIL_REQUIRED);
            emailInput.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            passwordInput.setError(Constants.ERROR_PASSWORD_TOO_SHORT);
            passwordInput.requestFocus();
            return;
        }

        LogUtils.d("LoginActivity", "Attempting login for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        LogUtils.d("LoginActivity", "Login successful");
                        navigateToDashboard();
                    } else {
                        String errorMessage = Constants.ERROR_UNKNOWN;
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            LogUtils.e("LoginActivity", "Login failed", task.getException());
                        }
                        Toast.makeText(loginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void navigateToDashboard() {
        Toast.makeText(loginActivity.this, Constants.SUCCESS_LOGIN, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(loginActivity.this, dashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void signInWithGoogle() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN);
        } catch (Exception e) {
            LogUtils.e("LoginActivity", "Google sign-in error", e);
            Toast.makeText(this, "Google Sign-In Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (TextUtils.isEmpty(idToken)) {
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        LogUtils.d("LoginActivity", "Google auth successful");
                        navigateToDashboard();
                    } else {
                        String errorMessage = "Google Auth Failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            LogUtils.e("LoginActivity", "Google auth failed", task.getException());
                        }
                        Toast.makeText(loginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null || TextUtils.isEmpty(token.getToken())) {
            Toast.makeText(this, "Facebook authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        LogUtils.d("LoginActivity", "Facebook auth successful");
                        navigateToDashboard();
                    } else {
                        String errorMessage = "Facebook Auth Failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            LogUtils.e("LoginActivity", "Facebook auth failed", task.getException());
                        }
                        Toast.makeText(loginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                LogUtils.e("LoginActivity", "Google sign-in exception", e);
                Toast.makeText(this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}