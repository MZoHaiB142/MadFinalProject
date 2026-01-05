package com.example.madfinalproject;

public class CommunityPost {
    String userName, time, role, title, body, category;
    int likeCount, commentCount;
    boolean isLiked;

    public CommunityPost(String userName, String time, String role, String title, String body, String category, int likeCount, int commentCount) {
        this.userName = userName;
        this.time = time;
        this.role = role;
        this.title = title;
        this.body = body;
        this.category = category; // e.g., "Visa Help", "General"
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLiked = false;
    }
}