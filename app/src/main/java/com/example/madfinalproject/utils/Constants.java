package com.example.madfinalproject.utils;

/**
 * Constants class to store all app-wide constants
 * This helps maintain consistency and makes the code more maintainable
 */
public class Constants {
    public static final String DB_VISA_INTERVIEW = "visaInterview";
    public static final String DB_VISA_INTERVIEW_QUESTIONS = "questions";
    public static final String EXTRA_INTERVIEW_COUNTRY = "interview_country";
    
    // Firebase Database References
    public static final String DB_USERS = "Users";
    public static final String DB_COMMUNITY_POSTS = "CommunityPosts";
    public static final String DB_COMMENTS = "Comments";
    public static final String DB_PROFILE_IMAGES = "ProfileImages";
    public static final String DB_UNIVERSITIES = "Universities";
    public static final String DB_SCHOLARSHIPS = "Scholarships";
    
    // User Data Keys
    public static final String KEY_UID = "uid";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public static final String KEY_QUALIFICATION = "qualification";
    public static final String KEY_TARGET_COUNTRIES = "targetCountries";
    public static final String KEY_INTERESTED_FIELDS = "interestedFields";
    public static final String KEY_UNIVERSITY_NAME = "universityName";
    public static final String KEY_COLLEGE_NAME = "collegeName";
    public static final String KEY_SCHOOL_NAME = "schoolName";
    public static final String KEY_LAST_GRADES = "lastGrades";
    public static final String KEY_CGPA = "cgpa";
    public static final String KEY_IELTS_SCORE = "bands";
    public static final String KEY_BUDGET = "budget";
    public static final String KEY_DEGREE_LEVEL = "degreeLevel";
    
    // Recommendation Categories
    public static final String CATEGORY_SAFE = "Safe";
    public static final String CATEGORY_TARGET = "Target";
    public static final String CATEGORY_AMBITIOUS = "Ambitious";
    
    // Degree Levels
    public static final String DEGREE_BACHELORS = "Bachelors";
    public static final String DEGREE_MASTERS = "Masters";
    
    // Request Codes
    public static final int RC_GOOGLE_SIGN_IN = 9001;
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    // Splash Screen Delay
    public static final int SPLASH_DELAY_MS = 1000;
    
    // Community Post Categories
    public static final String CATEGORY_GENERAL = "General";
    public static final String CATEGORY_VISA_HELP = "Visa Help";
    public static final String CATEGORY_IELTS_PREP = "IELTS Prep";
    public static final String CATEGORY_UNIVERSITY_LIFE = "University Life";
    
    // Error Messages
    public static final String ERROR_EMAIL_REQUIRED = "Email is required";
    public static final String ERROR_PASSWORD_REQUIRED = "Password is required";
    public static final String ERROR_PASSWORD_TOO_SHORT = "Password must be at least 6 characters";
    public static final String ERROR_NAME_REQUIRED = "Full Name is required";
    public static final String ERROR_PHONE_REQUIRED = "Phone is required";
    public static final String ERROR_TERMS_NOT_ACCEPTED = "Please agree to the Terms & Privacy Policy";
    public static final String ERROR_USER_NOT_LOGGED_IN = "User not logged in!";
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_UNKNOWN = "An unknown error occurred";
    
    // Success Messages
    public static final String SUCCESS_ACCOUNT_CREATED = "Account Created Successfully!";
    public static final String SUCCESS_LOGIN = "Login Successful!";
    public static final String SUCCESS_PROFILE_SAVED = "Profile Saved Successfully!";
    public static final String SUCCESS_PASSWORD_RESET_SENT = "Reset link sent to your email. Please check your inbox.";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
