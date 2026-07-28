package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
import com.example.madfinalproject.utils.ValidationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
// ✅ Firestore Import
import com.google.firebase.firestore.FirebaseFirestore;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.HashMap;

public class signupActivity extends AppCompatActivity {

    // UI Elements
    private EditText etFullName, etEmail, etPassword;
    private CheckBox cbTerms;

    // Firebase
    private FirebaseAuth mAuth;
    // ✅ Change 1: Firestore Variable
    private FirebaseFirestore db;

    // Social Login
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook SDK first
        com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());

        // Set layout only once
        setContentView(R.layout.signup);

        // 1. Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ✅ Change 2: Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // 2. Initialize UI elements
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Google OAuth Client ID from resources
        String googleClientId = getString(R.string.google_oauth_client_id);
        if (TextUtils.isEmpty(googleClientId)) {
            googleClientId = "744209892194-6k91hb0n24moamqap6acuhahv0kie1v4.apps.googleusercontent.com";
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mCallbackManager = CallbackManager.Factory.create();

        // 3. Set the Button Listener
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> validateAndRegister());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInWithGoogle());

        findViewById(R.id.btnFacebook).setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(signupActivity.this, Arrays.asList("email", "public_profile"));
        });
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(signupActivity.this, "Facebook Cancelled", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(signupActivity.this, "Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // SECTION 3: Google & Facebook Login Logic
    // ==========================================
    private void signInWithGoogle() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN);
        } catch (Exception e) {
            LogUtils.e("SignupActivity", "Google sign-in error", e);
            Toast.makeText(this, "Google Sign-In Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (TextUtils.isEmpty(idToken)) {
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String displayName = user.getDisplayName();
                    if (TextUtils.isEmpty(displayName)) {
                        displayName = "User"; // Default name
                    }
                    LogUtils.d("SignupActivity", "Google auth successful");
                    saveUserToDatabase(user, displayName);
                } else {
                    Toast.makeText(signupActivity.this, Constants.ERROR_USER_NOT_LOGGED_IN, Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMessage = "Google Auth Failed";
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage();
                    LogUtils.e("SignupActivity", "Google auth failed", task.getException());
                }
                Toast.makeText(signupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null || TextUtils.isEmpty(token.getToken())) {
            Toast.makeText(this, "Facebook authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String displayName = user.getDisplayName();
                    if (TextUtils.isEmpty(displayName)) {
                        displayName = "User"; // Default name
                    }
                    LogUtils.d("SignupActivity", "Facebook auth successful");
                    saveUserToDatabase(user, displayName);
                } else {
                    Toast.makeText(signupActivity.this, Constants.ERROR_USER_NOT_LOGGED_IN, Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMessage = "Facebook Auth Failed";
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage();
                    LogUtils.e("SignupActivity", "Facebook auth failed", task.getException());
                }
                Toast.makeText(signupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // SECTION 1: EMAIL & PASSWORD LOGIC
    // ==========================================
    private void validateAndRegister() {
        String fullName = ValidationUtils.trimString(etFullName.getText().toString());
        String email = ValidationUtils.trimString(etEmail.getText().toString());
        String password = ValidationUtils.trimString(etPassword.getText().toString());

        // Validation Logic
        if (!ValidationUtils.isValidName(fullName)) {
            etFullName.setError(Constants.ERROR_NAME_REQUIRED);
            etFullName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(Constants.ERROR_EMAIL_REQUIRED);
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError(Constants.ERROR_PASSWORD_TOO_SHORT);
            etPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, Constants.ERROR_TERMS_NOT_ACCEPTED, Toast.LENGTH_SHORT).show();
            return;
        }

        // If everything is good, create the user in Firebase Auth
        LogUtils.d("SignupActivity", "Attempting registration for: " + email);
        createUser(email, password, fullName);
    }

    private void createUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created in Auth, now save details to Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            LogUtils.d("SignupActivity", "User created successfully");
                            saveUserToDatabase(user, fullName);
                        } else {
                            Toast.makeText(signupActivity.this, Constants.ERROR_USER_NOT_LOGGED_IN, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If creation fails, show the error
                        String errorMessage = "Registration Failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            LogUtils.e("SignupActivity", "Registration failed", task.getException());
                        }
                        Toast.makeText(signupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ==========================================
    // SECTION 2: DATABASE SAVING & NAVIGATION (FIRESTORE)
    // ==========================================
    private void saveUserToDatabase(FirebaseUser user, String fullName) {
        if (user == null) {
            LogUtils.e("SignupActivity" + "User is null, cannot save to database");
            return;
        }

        String userId = user.getUid();
        String email = user.getEmail();

        // Prepare data using constants
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put(Constants.KEY_UID, userId);
        userMap.put(Constants.KEY_FULL_NAME, fullName);
        if (email != null) {
            userMap.put(Constants.KEY_EMAIL, email);
        }

        db.collection("Users").document(userId).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    LogUtils.d("SignupActivity", "User data saved to database");
                    Toast.makeText(signupActivity.this, Constants.SUCCESS_ACCOUNT_CREATED, Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard
                    Intent intent = new Intent(signupActivity.this, dashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Database Error: " + e.getMessage();
                    LogUtils.e("SignupActivity", "Database save failed", e);
                    Toast.makeText(signupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================
    // SECTION 4: ACTIVITY RESULT
    // ==========================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass result to Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Pass result to Google
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
                LogUtils.e("SignupActivity", "Google sign-in exception", e);
                Toast.makeText(this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}