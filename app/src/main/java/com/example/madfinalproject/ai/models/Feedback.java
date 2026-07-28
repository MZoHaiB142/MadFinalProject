package com.example.madfinalproject.ai.models;

public final class Feedback {
    private String message;
    public Feedback(String message){this.message=message;}
    public String getMessage(){return message == null ? "" : message;}
}
