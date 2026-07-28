package com.example.madfinalproject.models;

import com.google.firebase.Timestamp;

public class Consultant {

    private String    id;
    private String    name;
    private String    expertise;
    private double    rating;
    private boolean   isOnline;
    private String    photoUrl;
    private String    phone;
    private Timestamp lastSeen;

    // Firestore ke liye empty constructor
    public Consultant() {}

    // Existing constructor — backward compatible
    public Consultant(String id, String name, String expertise,
                      double rating, boolean isOnline, String photoUrl) {
        this.id        = id;
        this.name      = name;
        this.expertise = expertise;
        this.rating    = rating;
        this.isOnline  = isOnline;
        this.photoUrl  = photoUrl;
    }

    // Getters
    public String    getId()        { return id; }
    public String    getName()      { return name; }
    public String    getExpertise() { return expertise; }
    public double    getRating()    { return rating; }
    public boolean   isOnline()     { return isOnline; }
    public String    getPhotoUrl()  { return photoUrl; }
    public String    getPhone()     { return phone; }
    public Timestamp getLastSeen()  { return lastSeen; }

    // Setters
    public void setId(String v)        { id = v; }
    public void setName(String v)      { name = v; }
    public void setExpertise(String v) { expertise = v; }
    public void setRating(double v)    { rating = v; }
    public void setOnline(boolean v)   { isOnline = v; }
    public void setPhotoUrl(String v)  { photoUrl = v; }
    public void setPhone(String v)     { phone = v; }
    public void setLastSeen(Timestamp v){ lastSeen = v; }
}