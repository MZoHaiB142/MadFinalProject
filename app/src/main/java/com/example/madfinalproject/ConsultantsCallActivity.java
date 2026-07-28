package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ConsultantsCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        String consultantName  = getIntent().getStringExtra("CONSULTANT_NAME");
        String consultantPhone = getIntent().getStringExtra("CONSULTANT_PHONE");
        String consultantPhoto = getIntent().getStringExtra("CONSULTANT_PHOTO");

        TextView   tvName    = findViewById(R.id.tvCallingName);
        ImageView  ivPhoto   = findViewById(R.id.ivCallingPhoto);
        @SuppressLint("WrongViewCast") Button     btnCall   = findViewById(R.id.btnStartCall);
        @SuppressLint("WrongViewCast") Button     btnCancel = findViewById(R.id.btnCancelCall);

        tvName.setText(consultantName);

        if (consultantPhoto != null && !consultantPhoto.isEmpty()) {
            Glide.with(this).load(consultantPhoto)
                    .circleCrop().into(ivPhoto);
        }

        // Phone call karo
        btnCall.setOnClickListener(v -> {
            if (consultantPhone != null && !consultantPhone.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + consultantPhone));
                startActivity(callIntent);
            } else {
                Toast.makeText(this,
                        "Phone number is unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
