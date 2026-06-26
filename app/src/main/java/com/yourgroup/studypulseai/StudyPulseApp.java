package com.yourgroup.studypulseai;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class StudyPulseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        PDFBoxResourceLoader.init(getApplicationContext());
    }
}