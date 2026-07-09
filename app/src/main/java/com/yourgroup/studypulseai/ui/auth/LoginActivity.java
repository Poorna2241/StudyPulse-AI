package com.yourgroup.studypulseai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.yourgroup.studypulseai.MainActivity;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnSignIn).setOnClickListener(v -> signIn());
        findViewById(R.id.tvRegister).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> resetPassword());
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        
        SupabaseAuthHelper.signIn(email, pass, (success, error) -> {
            progressBar.setVisibility(View.GONE);
            if (success) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetPassword() {
        // Supabase password reset can be added later
        Toast.makeText(this, "Reset password feature coming soon", Toast.LENGTH_SHORT).show();
    }
}
