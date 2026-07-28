package com.example.madfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.adapters.ConsultantsChatListAdapter;
import com.example.madfinalproject.models.ConsultantsChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConsultantsChatlistActivity extends AppCompatActivity {

    // ── Views ──
    private RecyclerView              rvChatList;
    private ConsultantsChatListAdapter chatListAdapter;
    private LinearLayout              layoutEmpty;
    private ImageButton               btnBack, btnNewChat;
    private TextView                  tvTitle;

    // ── Data ──
    private final List<ConsultantsChatRoom> chatRoomList = new ArrayList<>();

    // ── Firebase ──
    private FirebaseFirestore    db;
    private FirebaseAuth         auth;
    private ListenerRegistration chatListListener;

    // ─────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultant_chat_list);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        loadChatRoomsRealtime();
    }

    // ─────────────────────────────────────────────
    // INIT VIEWS
    // ─────────────────────────────────────────────
    private void initViews() {
        rvChatList  = findViewById(R.id.rvChatList);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBack     = findViewById(R.id.btnBack);
        btnNewChat  = findViewById(R.id.btnNewChat);
        tvTitle     = findViewById(R.id.tvTitle);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // New chat button — consultant list par jao
        btnNewChat.setOnClickListener(v ->
                startActivity(
                        new Intent(this, LiveConsultantActivity.class))
        );
    }

    // ─────────────────────────────────────────────
    // RECYCLERVIEW SETUP
    // ─────────────────────────────────────────────
    private void setupRecyclerView() {
        chatListAdapter = new ConsultantsChatListAdapter(
                this,
                chatRoomList,
                room -> openChatScreen(room)
        );

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatListAdapter);
    }

    // ─────────────────────────────────────────────
    // FIREBASE REALTIME LISTENER
    // ─────────────────────────────────────────────
    private void loadChatRoomsRealtime() {
        String userId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : "";

        if (userId.isEmpty()) return;

        chatListListener = db.collection("chatRooms")
                .whereEqualTo("userId", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    chatRoomList.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ConsultantsChatRoom room = doc.toObject(ConsultantsChatRoom.class);
                            room.setChatRoomId(doc.getId());
                            chatRoomList.add(room);
                        }
                    }

                    // Empty state show/hide
                    updateEmptyState();

                    chatListAdapter.notifyDataSetChanged();
                });
    }

    // ─────────────────────────────────────────────
    // OPEN CHAT SCREEN
    // ─────────────────────────────────────────────
    private void openChatScreen(ConsultantsChatRoom room) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONSULTANT_ID",        room.getConsultantId());
        intent.putExtra("CONSULTANT_NAME",      room.getConsultantName());
        intent.putExtra("CONSULTANT_PHOTO",     room.getConsultantPhoto());
        intent.putExtra("CONSULTANT_EXPERTISE", room.getConsultantExpertise());
        intent.putExtra("IS_ONLINE",            room.isConsultantOnline());
        startActivity(intent);
    }

    // ─────────────────────────────────────────────
    // EMPTY STATE
    // ─────────────────────────────────────────────
    private void updateEmptyState() {
        boolean isEmpty = chatRoomList.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvChatList.setVisibility(isEmpty  ? View.GONE    : View.VISIBLE);
    }

    // ─────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListListener != null) {
            chatListListener.remove();
        }
    }
}