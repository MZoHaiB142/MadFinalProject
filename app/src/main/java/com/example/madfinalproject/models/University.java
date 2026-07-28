package com.example.madfinalproject.models;

import java.util.List;

public class University {
    public String id;
    public String name;
    public String location;
    public String ranking;
    public String acceptanceRate;
    public String fees;
    public String imageUrl;
    public String visaRatio;      // Percentage e.g., "85%"
    public int scholarshipCount;  // Number e.g., 18
    public int matchScore;        // Percentage e.g., 92
    public List<String> tags;
    public List<Scholarship> scholarships;
    public List<Program> programs;

    // Empty Constructor (Firebase ke liye Zaroori hai)
    public University() {}

    // Updated Constructor
    public University(String id, String name, String location, String ranking,
                      String acceptanceRate, String fees, String imageUrl,
                      String visaRatio, int scholarshipCount, int matchScore, List<String> tags) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.ranking = ranking;
        this.acceptanceRate = acceptanceRate;
        this.fees = fees;
        this.imageUrl = imageUrl;
        this.visaRatio = visaRatio;
        this.scholarshipCount = scholarshipCount;
        this.matchScore = matchScore;
        this.tags = tags;
    }
    // Getters and Setters (Firebase data mapping ke liye zaroori hain)
    public String getVisaRatio() { return visaRatio; }
    public void setVisaRatio(String visaRatio) { this.visaRatio = visaRatio; }

    public int getScholarshipCount() { return scholarshipCount; }
    public void setScholarshipCount(int scholarshipCount) { this.scholarshipCount = scholarshipCount; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public List<Scholarship> getScholarships() { return scholarships == null ? java.util.Collections.emptyList() : scholarships; }
    public void setScholarships(List<Scholarship> scholarships) { this.scholarships = scholarships; }
    public List<Program> getPrograms() { return programs == null ? java.util.Collections.emptyList() : programs; }
    public void setPrograms(List<Program> programs) { this.programs = programs; }

    public static class Scholarship {
        public String title, amount, deadline, link, eligibility;
        public Scholarship() {}
        public String getTitle(){return safe(title);} public String getAmount(){return safe(amount);}
        public String getDeadline(){return safe(deadline);} public String getLink(){return safe(link);}
        public String getEligibility(){return safe(eligibility);}
    }

    public static class Program {
        public String course_name, degree_level, yearly_fees, duration;
        public Program() {}
        public String getCourseName(){return safe(course_name);} public String getDegreeLevel(){return safe(degree_level);}
        public String getYearlyFees(){return safe(yearly_fees);} public String getDuration(){return safe(duration);}
    }

    private static String safe(String value){return value == null ? "" : value;}
}
