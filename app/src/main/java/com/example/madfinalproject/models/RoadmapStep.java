package com.example.madfinalproject.models;

import java.util.List;

public class RoadmapStep {
    public String id;
    public String phase_id;
    public int order;
    public String emoji;
    public String title;
    public String subtitle;
    public String status;
    public String howto_title;
    public List<String> howto_steps;
    public String tip;
    public String btn1_text;
    public String btn2_text;   // ← YEH ADD KARO
    public String sheet_type;

    public RoadmapStep() {}
}