package com.example.madfinalproject.utils;

import android.util.Log;


public class LogUtils {

    private static final String TAG = "AbroadIQ";
    private static final boolean DEBUG = true; // debug switch

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(TAG, "[" + tag + "] " + message);
        }
    }

    public static void e(String message) {
        if (DEBUG) {
            Log.e(TAG, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.e(TAG, "[" + tag + "] " + message, throwable);
        }
    }

    public static void i(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            Log.w(TAG, message);
        }
    }

    private LogUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
