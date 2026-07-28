package com.example.madfinalproject;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class CommunityPost {

    // --- VARIABLES ---
    public String postId;
    public String userId;    // My Posts ke liye zaroori
    public String userName;
    public String userEmail;
    public String role;
    public String title;
    public String body;      // Text yahan save hota hai
    public String category;
    public long timestamp;
    public Map<String, Boolean> likes = new HashMap<>();
    public int commentCount;

    @Exclude
    public boolean isLiked; // UI ke liye (Database mein nahi jayega)

    // --- CONSTRUCTORS ---

    // 1. Empty Constructor (Firebase ke liye)
    public CommunityPost() {}

    // 2. Constructor for Creating Post
    public CommunityPost(String userId, String userName, String userEmail, String role, String title, String body, String category) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.role = role;
        this.title = title;
        this.body = body;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
        this.likes = new HashMap<>();
        this.commentCount = 0;
        this.isLiked = false;
    }

    // --- HELPER METHODS ---

    // 🔥 Ye wo function hai jo missing tha
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

    // Like count helper
    @Exclude
    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }
}