package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.ConsultantAdapter;
import com.example.madfinalproject.models.Consultant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LiveConsultantActivity extends AppCompatActivity {

    private RecyclerView          rvConsultants;
    private ConsultantAdapter     consultantAdapter;
    private EditText              etSearchConsultant;
    private ProgressBar           progressLoading;
    private LinearLayout          layoutEmpty;
    private AppCompatImageButton  btnChatList;
    private TextView              tvUnreadBadge;

    private final List<Consultant> consultantList = new ArrayList<>();
    private final List<Consultant> filteredList   = new ArrayList<>();

    private FirebaseFirestore    db;
    private ListenerRegistration consultantsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_consultant);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupSearch();
        setupTopButtons();
        loadConsultantsRealtime();
    }

    private void initViews() {
        rvConsultants      = findViewById(R.id.rvConsultants);
        etSearchConsultant = findViewById(R.id.etSearchConsultant);
        progressLoading    = findViewById(R.id.progressLoading);
        layoutEmpty        = findViewById(R.id.layoutEmpty);
        btnChatList        = findViewById(R.id.btnChatList);
        tvUnreadBadge      = findViewById(R.id.tvUnreadBadge);
    }

    private void setupRecyclerView() {
        consultantAdapter = new ConsultantAdapter(
                this,
                filteredList,
                consultant -> openChat(consultant),
                consultant -> openCall(consultant)
        );
        rvConsultants.setLayoutManager(new LinearLayoutManager(this));
        rvConsultants.setAdapter(consultantAdapter);
    }

    private void setupTopButtons() {
        // 🔥 Fix applied: Going to correct Chat List Activity
        btnChatList.setOnClickListener(v -> {
            startActivity(new Intent(this, ConsultantsChatlistActivity.class));
        });
    }

    private void loadConsultantsRealtime() {
        showLoading(true);

        consultantsListener = db.collection("consultants")
                .addSnapshotListener((snapshots, error) -> {
                    showLoading(false);

                    if (error != null) {
                        showToast("Error: " + error.getMessage());
                        return;
                    }

                    consultantList.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Consultant c = doc.toObject(Consultant.class);
                            c.setId(doc.getId());
                            consultantList.add(c);
                        }
                    }
                    applyFilter(); // Search k hisab se update karega
                });
    }

    private void setupSearch() {
        etSearchConsultant.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        String query = etSearchConsultant.getText().toString().trim().toLowerCase();
        filteredList.clear();

        for (Consultant c : consultantList) {
            boolean matchSearch = query.isEmpty()
                    || c.getName().toLowerCase().contains(query)
                    || c.getExpertise().toLowerCase().contains(query);

            if (matchSearch) {
                filteredList.add(c);
            }
        }

        // Sort: Online walay pehle ayenge
        filteredList.sort((a, b) -> {
            if (a.isOnline() && !b.isOnline()) return -1;
            if (!a.isOnline() && b.isOnline()) return 1;
            return Double.compare(b.getRating(), a.getRating());
        });

        layoutEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        rvConsultants.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        consultantAdapter.notifyDataSetChanged();
    }

    private void openChat(Consultant c) {
        // 🔥 Fix applied: Going to correct Chat Activity
        Intent intent = new Intent(this, ConsultantsChatActivity.class);
        intent.putExtra("CONSULTANT_ID",        c.getId());
        intent.putExtra("CONSULTANT_NAME",      c.getName());
        intent.putExtra("CONSULTANT_PHOTO",     c.getPhotoUrl());
        intent.putExtra("CONSULTANT_EXPERTISE", c.getExpertise());
        intent.putExtra("IS_ONLINE",            c.isOnline());
        startActivity(intent);
    }

    private void openCall(Consultant c) {
        // 🔥 Fix applied: Going to correct Call Activity
        Intent intent = new Intent(this, ConsultantCallActivity.class);
        intent.putExtra("CONSULTANT_ID",        c.getId());
        intent.putExtra("CONSULTANT_NAME",      c.getName());
        intent.putExtra("CONSULTANT_PHONE",     c.getPhone());
        intent.putExtra("CONSULTANT_PHOTO",     c.getPhotoUrl());
        intent.putExtra("CONSULTANT_EXPERTISE", c.getExpertise());
        intent.putExtra("CONSULTANT_RATING",    c.getRating());
        intent.putExtra("IS_ONLINE",            c.isOnline());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        rvConsultants.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void updateUnreadBadge(int count) {
        if (count > 0) {
            tvUnreadBadge.setVisibility(View.VISIBLE);
            tvUnreadBadge.setText(count > 9 ? "9+" : String.valueOf(count));
        } else {
            tvUnreadBadge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (consultantsListener != null) {
            consultantsListener.remove();
        }
    }
}