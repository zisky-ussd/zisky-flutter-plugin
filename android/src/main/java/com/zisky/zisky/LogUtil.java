package com.zisky.zisky;

import android.util.Log;

public class LogUtil {
    public static final String LOG_TAG = "zisky-flutter-library";

    public static void e(Exception e) {
        Log.e(LOG_TAG, "Exception occurred", e);
    }

    public static void e(String message, Exception e) {
        Log.e(LOG_TAG, message, e);
    }

    public static void w(String message, Exception e) {
        Log.w(LOG_TAG, message, e);
    }

    public static void w(String message) {
        Log.w(LOG_TAG, message);
    }

    public static void w(Exception e) {
        Log.w(LOG_TAG, "Exception occurred", e);
    }

    public static void d(String message) {
        Log.d(LOG_TAG, message);
    }

}
