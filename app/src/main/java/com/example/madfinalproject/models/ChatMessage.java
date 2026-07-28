package com.example.madfinalproject.models;

/**
 * Model class for chat messages
 */
public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_LOADING = 2;
    public static final int TYPE_ERROR = 3;

    private String message;
    private int type;
    private long timestamp;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUser() {
        return type == TYPE_USER;
    }

    public boolean isBot() {
        return type == TYPE_BOT;
    }

    public boolean isLoading() {
        return type == TYPE_LOADING;
    }

    public boolean isError() {
        return type == TYPE_ERROR;
    }
}
