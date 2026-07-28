package com.example.madfinalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.madfinalproject.adapters.ChatAdapter;
import com.example.madfinalproject.models.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.android.volley.BuildConfig;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView btnBack;

    private ChatAdapter chatAdapter;
    private RequestQueue requestQueue;

    // 🔑 API KEY (Apni key yahan lagayen)
    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-3.5-flash:generateContent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);


        chatAdapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);


        requestQueue = Volley.newRequestQueue(this);


        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }


        btnSend.setOnClickListener(v -> sendMessage());


        addWelcomeMessage();
    }

    private void addWelcomeMessage() {
        String welcome = "Hello! I am AbroadIQ Assistant. How can I help you with your study abroad plans today?";
        chatAdapter.addMessage(new ChatMessage(welcome, ChatMessage.TYPE_BOT));
    }

    private void sendMessage() {
        String userMsg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(userMsg)) return;
        chatAdapter.addMessage(new ChatMessage(userMsg, ChatMessage.TYPE_USER));
        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        etMessage.setText("");

        chatAdapter.addMessage(new ChatMessage("", ChatMessage.TYPE_LOADING));
        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        // API Calling
        sendToAiService(userMsg);
    }

    private void sendToAiService(String userPrompt) {

        if (com.example.madfinalproject.BuildConfig.GEMINI_API_KEY == null
                || com.example.madfinalproject.BuildConfig.GEMINI_API_KEY.trim().isEmpty()) {
            chatAdapter.removeLoadingMessage();
            chatAdapter.addMessage(new ChatMessage(
                    "AI service is not configured.", ChatMessage.TYPE_ERROR));
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            String fullPrompt = "You are AbroadIQ, a helpful study-abroad assistant. "
                    + "Give accurate, concise and professional answers in English. "
                    + "User message: " + userPrompt;

            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", fullPrompt));
            JSONObject content = new JSONObject()
                    .put("role", "user")
                    .put("parts", parts);
            jsonBody.put("contents", new JSONArray().put(content));
            jsonBody.put("generationConfig", new JSONObject()
                    .put("temperature", 0.4)
                    .put("maxOutputTokens", 800));

        } catch (JSONException e) {
            chatAdapter.removeLoadingMessage();
            chatAdapter.addMessage(new ChatMessage(
                    "Unable to prepare your message.", ChatMessage.TYPE_ERROR));
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Loading hatao
                        chatAdapter.removeLoadingMessage();

                        try {
                            String botReply = response.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            chatAdapter.addMessage(new ChatMessage(botReply, ChatMessage.TYPE_BOT));
                            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

                        } catch (JSONException e) {
                            chatAdapter.addMessage(new ChatMessage("Error parsing: " + e.getMessage(), ChatMessage.TYPE_ERROR));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Loading hatao
                        chatAdapter.removeLoadingMessage();

                        String errorMsg = "Unable to contact the AI service.";
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400) {
                                errorMsg = "The AI could not process this message.";
                            } else if (statusCode == 403) {
                                errorMsg = "The Gemini API key does not have permission.";
                            } else if (statusCode == 404) {
                                errorMsg = "The selected Gemini model is unavailable.";
                            } else if (statusCode == 429) {
                                errorMsg = "The AI request limit was reached. Please try again shortly.";
                            } else if (statusCode >= 500) {
                                errorMsg = "Gemini is temporarily unavailable. Please try again.";
                            } else {
                                errorMsg = "AI service error (" + statusCode + ").";
                            }
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        chatAdapter.addMessage(new ChatMessage(errorMsg, ChatMessage.TYPE_ERROR));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-goog-api-key",
                        com.example.madfinalproject.BuildConfig.GEMINI_API_KEY);
                return headers;
            }
        };

        // Timeout badhana (AI slow ho sakta ha)
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 Seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Queue mein add karo
        jsonObjectRequest.setTag(this);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null) requestQueue.cancelAll(this);
        super.onDestroy();
    }
}
