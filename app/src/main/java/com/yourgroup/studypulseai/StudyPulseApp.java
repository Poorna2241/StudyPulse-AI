package com.yourgroup.studypulseai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.FirebaseApp;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class StudyPulseApp extends Application {
    private static StudyPulseApp instance;

    public static StudyPulseApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        PDFBoxResourceLoader.init(getApplicationContext());

        // Apply saved theme preference on startup
        SharedPreferences prefs = getSharedPreferences("StudyPulsePrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
