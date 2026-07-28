package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Use Handler with Looper for better compatibility
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            LogUtils.d("MainActivity", "Navigating to login screen");
            Intent intent = new Intent(MainActivity.this, loginActivity.class);
            startActivity(intent);
            finish(); // Close splash screen so user can't go back
        }, Constants.SPLASH_DELAY_MS);
    }
}