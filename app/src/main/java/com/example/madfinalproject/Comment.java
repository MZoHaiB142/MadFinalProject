package com.example.madfinalproject;

// ✅ Firestore Annotations Imports
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Comment {
    public String commentId;
    public String postId;
    public String userId;
    public String userName;
    public String userEmail;
    public String text;
    public long timestamp;

    // Default constructor for Firebase
    public Comment() {
    }

    // Constructor for creating new comments
    public Comment(String postId, String userId, String userName, String userEmail, String text) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    // ✅ @Exclude lagayen taake ye database mein save na ho
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("commentId", commentId);
        map.put("postId", postId);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("userEmail", userEmail);
        map.put("text", text);
        map.put("timestamp", timestamp);
        return map;
    }

    // ✅ @Exclude zaroori hai, warna Firestore isay "timeAgo" field samajh kar save kar lega
    @Exclude
    public String getTimeAgo() {
        if (timestamp == 0) return "Just now";
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }
}