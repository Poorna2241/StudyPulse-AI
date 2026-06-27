package com.yourgroup.studypulseai.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.ui.auth.LoginActivity;

public class SettingsFragment extends Fragment {
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile, btnChangePassword, btnSignOut, btnDeleteAccount;
    private AutoCompleteTextView spinnerAcademicLevel;
    private TextInputEditText etPrimarySubjects;
    private TextView tvStudyGoalValue;
    private Slider sliderStudyGoal;
    private MaterialSwitch switchReminders, switchDarkMode;
    
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireActivity().getSharedPreferences("StudyPulsePrefs", Context.MODE_PRIVATE);

        initViews(view);
        setupUserData();
        setupSpinners();
        loadPreferences();
        setupListeners();

        return view;
    }

    private void initViews(View v) {
        tvUserName = v.findViewById(R.id.tvUserName);
        tvUserEmail = v.findViewById(R.id.tvUserEmail);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        btnChangePassword = v.findViewById(R.id.btnChangePassword);
        btnSignOut = v.findViewById(R.id.btnSignOut);
        btnDeleteAccount = v.findViewById(R.id.btnDeleteAccount);
        spinnerAcademicLevel = v.findViewById(R.id.spinnerAcademicLevel);
        etPrimarySubjects = v.findViewById(R.id.etPrimarySubjects);
        tvStudyGoalValue = v.findViewById(R.id.tvStudyGoalValue);
        sliderStudyGoal = v.findViewById(R.id.sliderStudyGoal);
        switchReminders = v.findViewById(R.id.switchReminders);
        switchDarkMode = v.findViewById(R.id.switchDarkMode);
    }

    private void setupUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : getString(R.string.default_user_name));
            tvUserEmail.setText(user.getEmail());
        }
    }

    private void setupSpinners() {
        String[] levels = {"High School", "University", "Professional Certification", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, levels);
        spinnerAcademicLevel.setAdapter(adapter);
    }

    private void loadPreferences() {
        spinnerAcademicLevel.setText(prefs.getString("academic_level", "University"), false);
        etPrimarySubjects.setText(prefs.getString("primary_subjects", ""));
        float goal = prefs.getFloat("study_goal", 30f);
        sliderStudyGoal.setValue(goal);
        tvStudyGoalValue.setText(String.valueOf((int) goal));
        switchReminders.setChecked(prefs.getBoolean("reminders_enabled", true));
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
    }

    private void setupListeners() {
        btnSignOut.setOnClickListener(v -> signOut());
        
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Logic to delete from Firebase...
                    Toast.makeText(getContext(), "Account deletion initiated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? 
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Save AI settings when they change
        sliderStudyGoal.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putFloat("study_goal", value).apply();
            tvStudyGoalValue.setText(String.valueOf((int) value));
        });

        switchReminders.setOnCheckedChangeListener((button, isChecked) -> 
            prefs.edit().putBoolean("reminders_enabled", isChecked).apply());
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save text fields on pause
        prefs.edit()
            .putString("academic_level", spinnerAcademicLevel.getText().toString())
            .putString("primary_subjects", etPrimarySubjects.getText().toString())
            .apply();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}