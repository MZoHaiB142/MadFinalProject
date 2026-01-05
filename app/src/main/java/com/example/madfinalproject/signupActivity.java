package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference mDatabase;
    // Social Login
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private CallbackManager mCallbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.signup);
        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        // Note: Make sure your database rules allow writing!
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("744209892194-6k91hb0n24moamqap6acuhahv0kie1v4.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mCallbackManager = CallbackManager.Factory.create();

        // 3. Set the Button Listener (ONLY ONCE)
        // We call validateAndRegister, which will then call createUser if everything is okay.
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
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                // Save using Google Display Name
                saveUserToDatabase(user, user.getDisplayName());
            } else {
                Toast.makeText(signupActivity.this, "Google Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                // Save using Facebook Name
                saveUserToDatabase(user, user.getDisplayName());
            } else {
                Toast.makeText(signupActivity.this, "Facebook Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // SECTION 1: EMAIL & PASSWORD LOGIC
    // ==========================================
    private void validateAndRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 4. Validation Logic
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. If everything is good, create the user in Firebase Auth
        createUser(email, password, fullName);
    }

    private void createUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created in Auth, now save details to Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToDatabase(user, fullName);
                    } else {
                        // If creation fails, show the error
                        Toast.makeText(signupActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ==========================================
    // SECTION 2: DATABASE SAVING & NAVIGATION
    // ==========================================
    private void saveUserToDatabase(FirebaseUser user, String fullName) {
        // FIXED: Changed '!=' to '=='
        if (user == null) return;

        String userId = user.getUid();
        String email = user.getEmail();

        // Prepare data
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", userId);
        userMap.put("fullName", fullName);
        userMap.put("email", email);

        // Save to Database
        mDatabase.child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(signupActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to Dashboard or Login
                        Intent intent = new Intent(signupActivity.this, dashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(signupActivity.this, "Database Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}