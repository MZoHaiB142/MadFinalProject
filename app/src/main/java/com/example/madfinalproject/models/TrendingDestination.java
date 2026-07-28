package com.example.madfinalproject.models;

public final class TrendingDestination {
    private final String country;
    private final String imageUrl;
    private final int visaRatio;
    private final int universityCount;

    public TrendingDestination(String country, String imageUrl, int visaRatio, int universityCount) {
        this.country = country;
        this.imageUrl = imageUrl;
        this.visaRatio = visaRatio;
        this.universityCount = universityCount;
    }

    public String getCountry() { return country; }
    public String getImageUrl() { return imageUrl; }
    public int getVisaRatio() { return visaRatio; }
    public int getUniversityCount() { return universityCount; }
}
