package com.example.madfinalproject.reports;
public final class ReportStore { private static InterviewReport current; private ReportStore(){} public static synchronized void set(InterviewReport report){current=report;} public static synchronized InterviewReport get(){return current;} public static synchronized void clear(){current=null;} }
