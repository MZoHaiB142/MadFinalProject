package com.example.madfinalproject.models;

public class SopUniversity {

    private String name;
    private String city;
    private String country;
    private String logoUrl;
    private String documentId; // Firestore doc ID

    public SopUniversity() {}

    // Getters
    public String getName()       { return name; }
    public String getCity()       { return city; }
    public String getCountry()    { return country; }
    public String getLogoUrl()    { return logoUrl; }
    public String getDocumentId() { return documentId; }

    // Setters
    public void setName(String v)       { name = v; }
    public void setCity(String v)       { city = v; }
    public void setCountry(String v)    { country = v; }
    public void setLogoUrl(String v)    { logoUrl = v; }
    public void setDocumentId(String v) { documentId = v; }
}