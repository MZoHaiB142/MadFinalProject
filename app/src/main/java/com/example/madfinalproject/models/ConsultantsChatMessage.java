package com.example.madfinalproject.models;

import com.google.firebase.Timestamp;

public class ConsultantsChatMessage {

    private String    messageId;
    private int type;
    private String    senderId;
    private String    text;
    private Timestamp timestamp;
    private boolean   isRead;

    public ConsultantsChatMessage() {}

    public ConsultantsChatMessage(String senderId, String text) {
        this.senderId  = senderId;
        this.text      = text;
        this.timestamp = Timestamp.now();
        this.isRead    = false;
    }

    // Getters
    public String    getMessageId()  { return messageId; }
    public String    getSenderId()   { return senderId; }
    public String    getText()       { return text; }
    public Timestamp getTimestamp()  { return timestamp; }
    public boolean   isRead()        { return isRead; }

    // Setters
    public void setMessageId(String v)  { messageId = v; }
    public void setSenderId(String v)   { senderId = v; }
    public void setText(String v)       { text = v; }
    public void setTimestamp(Timestamp v){ timestamp = v; }
    public void setRead(boolean v)      { isRead = v; }
}