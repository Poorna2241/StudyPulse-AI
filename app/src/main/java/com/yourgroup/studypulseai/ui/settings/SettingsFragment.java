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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import com.yourgroup.studypulseai.ui.auth.LoginActivity;

public class SettingsFragment extends Fragment {
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile, btnChangePassword, btnSignOut, btnDeleteAccount;
    private AutoCompleteTextView spinnerAcademicLevel;
    private TextInputEditText etPrimarySubjects;
    private TextView tvStudyGoalValue;
    private Slider sliderStudyGoal;
    private SwitchMaterial switchReminders, switchDarkMode;
    private View rowReminders, rowDarkMode;
    
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
        rowReminders = v.findViewById(R.id.rowReminders);
        rowDarkMode = v.findViewById(R.id.rowDarkMode);
    }

    private void setupUserData() {
        String email = SupabaseAuthHelper.getCurrentUserEmail();
        String name = SupabaseAuthHelper.getCurrentUserName();
        
        if (email != null) {
            if (name == null || name.isEmpty() || name.equals("null")) {
                name = email.contains("@") ? email.split("@")[0] : email;
            }
            tvUserName.setText(name);
            tvUserEmail.setText(email);
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
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnSignOut.setOnClickListener(v -> signOut());
        
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancel", null)
                .show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean current = prefs.getBoolean("dark_mode", false);
            if (current != isChecked) {
                prefs.edit().putBoolean("dark_mode", isChecked).apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        rowDarkMode.setOnClickListener(v -> switchDarkMode.setChecked(!switchDarkMode.isChecked()));
        rowReminders.setOnClickListener(v -> switchReminders.setChecked(!switchReminders.isChecked()));

        // Save AI settings when they change
        sliderStudyGoal.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putFloat("study_goal", value).apply();
            tvStudyGoalValue.setText(String.valueOf((int) value));
        });

        switchReminders.setOnCheckedChangeListener((button, isChecked) -> 
            prefs.edit().putBoolean("reminders_enabled", isChecked).apply());
    }

    private void deleteUserAccount() {
        new Thread(() -> {
            try {
                com.yourgroup.studypulseai.data.db.AppDatabase.getInstance(requireContext()).clearAllTables();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        SupabaseAuthHelper.signOut(success -> {
                            Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            if (getActivity() != null) getActivity().finish();
                        });
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void showEditProfileDialog() {
        TextInputEditText etNewName = new TextInputEditText(requireContext());
        etNewName.setHint("Enter new name");
        etNewName.setText(tvUserName.getText());

        new AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(etNewName)
            .setPositiveButton("Update", (dialog, which) -> {
                String name = etNewName.getText().toString().trim();
                if (!name.isEmpty()) {
                    SupabaseAuthHelper.updateProfileName(name, (success, error) -> {
                        if (success) {
                            tvUserName.setText(name);
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showChangePasswordDialog() {
        TextInputEditText etNewPass = new TextInputEditText(requireContext());
        etNewPass.setHint("Enter new password");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(etNewPass)
            .setPositiveButton("Change", (dialog, which) -> {
                String pass = etNewPass.getText().toString().trim();
                if (pass.length() >= 6) {
                    SupabaseAuthHelper.changePassword(pass, (success, error) -> {
                        if (success) {
                            Toast.makeText(getContext(), "Password changed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Password too short", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
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
        SupabaseAuthHelper.signOut(success -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }
}
