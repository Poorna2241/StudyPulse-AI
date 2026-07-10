package com.yourgroup.studypulseai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.yourgroup.studypulseai.R;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        
        try {
            setContentView(R.layout.activity_welcome);
            Log.d(TAG, "Layout set successfully");

            MaterialButton btnSignUp = findViewById(R.id.btnSignUp);
            MaterialButton btnSignIn = findViewById(R.id.btnSignIn);

            btnSignUp.setOnClickListener(v -> {
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            });

            btnSignIn.setOnClickListener(v -> {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }
}
