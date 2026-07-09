package com.yourgroup.studypulseai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import com.yourgroup.studypulseai.ui.auth.WelcomeActivity;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starting");

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        try {
            if (!SupabaseAuthHelper.isLoggedIn()) {
                Log.d(TAG, "User not logged in, redirecting to WelcomeActivity");
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
                return;
            }

            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout set successfully");

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
                NavigationUI.setupWithNavController(bottomNav, navController);
                Log.d(TAG, "Navigation setup complete");
            } else {
                Log.e(TAG, "NavHostFragment is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }
}
