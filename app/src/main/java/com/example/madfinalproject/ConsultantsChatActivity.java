package com.example.madfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.adapters.ConsultantChatMessageAdapter;
import com.example.madfinalproject.models.ConsultantsChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsultantsChatActivity extends AppCompatActivity {

    // Views
    private RecyclerView    rvMessages;
    private EditText        etMessage;
    private ImageButton     btnSend, btnBack, btnCall;
    private TextView        tvConsultantName, tvOnlineStatus;
    private ImageView       ivConsultantPhoto, ivOnlineDot;
    private View            typingIndicator;

    // Adapter & Data
    private ConsultantChatMessageAdapter adapter;
    private List<ConsultantsChatMessage>  messageList = new ArrayList<>();

    // Firebase
    private FirebaseFirestore    db;
    private FirebaseAuth         auth;
    private ListenerRegistration messagesListener;
    private ListenerRegistration onlineListener;

    // Chat info
    private String consultantId;
    private String consultantName;
    private String consultantPhoto;
    private String consultantExpertise;
    private boolean isOnline;
    private String  chatRoomId;
    private String  currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Intent data receive karo
        consultantId        = getIntent().getStringExtra("CONSULTANT_ID");
        consultantName      = getIntent().getStringExtra("CONSULTANT_NAME");
        consultantPhoto     = getIntent().getStringExtra("CONSULTANT_PHOTO");
        consultantExpertise = getIntent().getStringExtra("CONSULTANT_EXPERTISE");
        isOnline            = getIntent().getBooleanExtra("IS_ONLINE", false);

        currentUserId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : "anonymous";

        // ChatRoom ID = userId + consultantId (sorted for consistency)
        chatRoomId = buildChatRoomId(currentUserId, consultantId);

        initViews();
        setupHeader();
        setupMessageInput();
        setupButtons();

        // Firebase listeners start karo
        listenForMessages();
        listenForOnlineStatus();
        markMessagesAsRead();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        rvMessages          = findViewById(R.id.rvMessages);
        etMessage           = findViewById(R.id.etMessage);
        btnSend             = findViewById(R.id.btnSend);
        btnBack             = findViewById(R.id.btnBack);
        btnCall             = findViewById(R.id.btnCall);
        tvConsultantName    = findViewById(R.id.tvConsultantName);
        tvOnlineStatus      = findViewById(R.id.tvOnlineStatus);
        ivConsultantPhoto   = findViewById(R.id.ivConsultantPhoto);
        ivOnlineDot         = findViewById(R.id.ivOnlineDot);
        typingIndicator     = findViewById(R.id.typingIndicator);

        // RecyclerView setup
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // Messages neeche se start hon
        rvMessages.setLayoutManager(lm);

        adapter = new ConsultantChatMessageAdapter(messageList, currentUserId);
        rvMessages.setAdapter(adapter);
    }

    private void setupHeader() {
        tvConsultantName.setText(consultantName);

        // Online status
        updateOnlineStatus(isOnline);

        // Profile photo
        if (consultantPhoto != null && !consultantPhoto.isEmpty()) {
            Glide.with(this)
                    .load(consultantPhoto)
                    .circleCrop()
                    .placeholder(R.drawable.ic_university)
                    .into(ivConsultantPhoto);
        }
    }

    private void updateOnlineStatus(boolean online) {
        if (online) {
            tvOnlineStatus.setText("Online");
            tvOnlineStatus.setTextColor(
                    getResources().getColor(R.color.green, null));
            ivOnlineDot.setVisibility(View.VISIBLE);
        } else {
            tvOnlineStatus.setText("Offline");
            tvOnlineStatus.setTextColor(0xFF9E9E9E);
            ivOnlineDot.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnCall.setOnClickListener(v -> {
            if (!isOnline) {
                Toast.makeText(this,
                        consultantName + " abhi offline hai",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ConsultantsCallActivity.class);
            intent.putExtra("CONSULTANT_ID",   consultantId);
            intent.putExtra("CONSULTANT_NAME", consultantName);
            startActivity(intent);
        });
    }

    // ─────────────────────────────────────────────
    // Message Input — Send button show/hide
    // ─────────────────────────────────────────────
    private void setupMessageInput() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // ─────────────────────────────────────────────
    // SEND MESSAGE
    // ─────────────────────────────────────────────
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText(""); // Input clear karo

        ConsultantsChatMessage msg = new ConsultantsChatMessage(currentUserId, text);

        // Firestore mein save karo
        db.collection("messages")
                .document(chatRoomId)
                .collection("chats")
                .add(msg)
                .addOnSuccessListener(docRef -> {
                    // ChatRoom update karo (last message)
                    updateChatRoom(text);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Unable to send message", Toast.LENGTH_SHORT).show()
                );
    }

    // ─────────────────────────────────────────────
    // UPDATE CHATROOM (last message, unread count)
    // ─────────────────────────────────────────────
    private void updateChatRoom(String lastMessage) {
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("userId",              currentUserId);
        chatRoomData.put("consultantId",        consultantId);
        chatRoomData.put("consultantName",      consultantName);
        chatRoomData.put("consultantPhoto",     consultantPhoto != null ? consultantPhoto : "");
        chatRoomData.put("consultantExpertise", consultantExpertise != null ? consultantExpertise : "");
        chatRoomData.put("lastMessage",         lastMessage);
        chatRoomData.put("lastMessageTime",     Timestamp.now());
        chatRoomData.put("unreadCount",         0); // User ne message bheja, unread 0

        db.collection("chatRooms")
                .document(chatRoomId)
                .set(chatRoomData);
    }

    // ─────────────────────────────────────────────
    // REALTIME MESSAGES LISTENER
    // ─────────────────────────────────────────────
    private void listenForMessages() {
        messagesListener = db.collection("messages")
                .document(chatRoomId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    messageList.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ConsultantsChatMessage msg = doc.toObject(ConsultantsChatMessage.class);
                            msg.setMessageId(doc.getId());
                            messageList.add(msg);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Scroll to bottom
                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    // ─────────────────────────────────────────────
    // REALTIME ONLINE STATUS LISTENER
    // ─────────────────────────────────────────────
    private void listenForOnlineStatus() {
        if (consultantId == null) return;

        onlineListener = db.collection("consultants")
                .document(consultantId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;

                    Boolean online = doc.getBoolean("isOnline");
                    isOnline = online != null && online;
                    runOnUiThread(() -> updateOnlineStatus(isOnline));
                });
    }

    // ─────────────────────────────────────────────
    // MARK MESSAGES AS READ
    // ─────────────────────────────────────────────
    private void markMessagesAsRead() {
        // Unread count reset karo
        db.collection("chatRooms")
                .document(chatRoomId)
                .update("unreadCount", 0)
                .addOnFailureListener(e -> { /* ignore */ });
    }

    // ─────────────────────────────────────────────
    // ChatRoom ID build karo — consistent order mein
    // ─────────────────────────────────────────────
    private String buildChatRoomId(String userId, String consultantId) {
        // Alphabetically sort karo — same room ID hamesha
        if (userId.compareTo(consultantId) < 0) {
            return userId + "_" + consultantId;
        }
        return consultantId + "_" + userId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) messagesListener.remove();
        if (onlineListener != null)   onlineListener.remove();
    }
}
