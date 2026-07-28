package com.example.madfinalproject.models;

import com.google.firebase.Timestamp;

public class ConsultantsChatRoom {

    private String    chatRoomId;
    private String    userId;
    private String    consultantId;
    private String    consultantName;
    private String    consultantPhoto;
    private String    consultantExpertise;
    private boolean   consultantOnline;
    private String    lastMessage;
    private Timestamp lastMessageTime;
    private int       unreadCount;

    public ConsultantsChatRoom() {}

    // Getters
    public String    getChatRoomId()          { return chatRoomId; }
    public String    getUserId()              { return userId; }
    public String    getConsultantId()        { return consultantId; }
    public String    getConsultantName()      { return consultantName; }
    public String    getConsultantPhoto()     { return consultantPhoto; }
    public String    getConsultantExpertise() { return consultantExpertise; }
    public boolean   isConsultantOnline()     { return consultantOnline; }
    public String    getLastMessage()         { return lastMessage; }
    public Timestamp getLastMessageTime()     { return lastMessageTime; }
    public int       getUnreadCount()         { return unreadCount; }

    // Setters
    public void setChatRoomId(String v)          { chatRoomId = v; }
    public void setUserId(String v)              { userId = v; }
    public void setConsultantId(String v)        { consultantId = v; }
    public void setConsultantName(String v)      { consultantName = v; }
    public void setConsultantPhoto(String v)     { consultantPhoto = v; }
    public void setConsultantExpertise(String v) { consultantExpertise = v; }
    public void setConsultantOnline(boolean v)   { consultantOnline = v; }
    public void setLastMessage(String v)         { lastMessage = v; }
    public void setLastMessageTime(Timestamp v)  { lastMessageTime = v; }
    public void setUnreadCount(int v)            { unreadCount = v; }
}