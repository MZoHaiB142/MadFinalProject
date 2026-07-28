package com.example.madfinalproject.models;
public class ScholarshipModel {
    String id;
    // Purane variables
    String title, university, country, deadline, start_date, link, source;

    // ✅ Naye Variables (Jo humne add kiye)
    String image_url;
    String amount;
    String targetDegree;

    // 1. Empty Constructor (Firebase ke liye Zaroori hai)
    public ScholarshipModel() {
    }

    // 2. Full Constructor (Data set karne ke liye)
    public ScholarshipModel(String title, String university, String country, String deadline, String start_date, String link, String source, String image_url, String amount) {
        this.title = title;
        this.university = university;
        this.country = country;
        this.deadline = deadline;
        this.start_date = start_date;
        this.link = link;
        this.source = source;
        this.image_url = image_url;
        this.amount = amount;
    }

    // 3. GETTERS (Adapter inhein use karta hai)

    public String getTitle() { return title; }

    public String getUniversity() { return university; }

    public String getCountry() { return country; }

    public String getDeadline() { return deadline; }

    public String getStart_date() { return start_date; }

    public String getLink() { return link; }

    public String getSource() { return source; }

    // ✅ Naye Getters (Inke bina error aayega)
    public String getImage_url() { return image_url; }

    public String getAmount() { return amount; }
    public String getId() { return id == null || id.isEmpty() ? String.valueOf((getTitle()+getUniversity()+getLink()).hashCode()) : id; }
    public void setId(String id) { this.id = id; }

    public String getTargetDegree() { return targetDegree == null ? "" : targetDegree; }

    public void setTargetDegree(String targetDegree) { this.targetDegree = targetDegree; }
}
