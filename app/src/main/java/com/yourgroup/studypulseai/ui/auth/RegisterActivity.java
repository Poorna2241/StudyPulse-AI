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

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
        findViewById(R.id.tvSignIn).setOnClickListener(v -> finish());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String conf = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(conf)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        SupabaseAuthHelper.signUp(email, pass, name, (success, error) -> {
            progressBar.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(this, "Registration successful! Please check your email.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
