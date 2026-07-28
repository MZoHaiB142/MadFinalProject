package com.example.madfinalproject.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() >= 10;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static String trimString(String input) {
        return input != null ? input.trim() : "";
    }

    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9+]", "");
    }
    
    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static String getOrDefault(String text, String fallback) {
        return text != null ? text : fallback;
    }

    private ValidationUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
