package com.yourgroup.studypulseai.ui.challenge;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.network.GeminiApiService;
import com.yourgroup.studypulseai.network.SupabaseRepo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CreateChallengeFragment extends Fragment {
    private TextView tvDeckName, tvDurationValue;
    private RadioGroup rgQuestionCount;
    private ChipGroup cgDifficulty;
    private Slider sliderDuration;
    private MaterialButton btnSelectTime, btnCreateChallenge;
    
    private int deckId = -1;
    private String deckTitle = "";
    private long selectedStartTime = -1;
    private final Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_challenge, container, false);

        if (getArguments() != null) {
            deckId = getArguments().getInt("deckId", -1);
            deckTitle = getArguments().getString("deckTitle", "Deck");
        }

        tvDeckName = view.findViewById(R.id.tvDeckName);
        tvDurationValue = view.findViewById(R.id.tvDurationValue);
        rgQuestionCount = view.findViewById(R.id.rgQuestionCount);
        cgDifficulty = view.findViewById(R.id.cgDifficulty);
        sliderDuration = view.findViewById(R.id.sliderDuration);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnCreateChallenge = view.findViewById(R.id.btnCreateChallenge);

        tvDeckName.setText(deckTitle);

        sliderDuration.addOnChangeListener((slider, value, fromUser) -> {
            tvDurationValue.setText((int) value + " mins");
        });

        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnCreateChallenge.setOnClickListener(v -> generateAndCreateChallenge());

        return view;
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            selectedStartTime = calendar.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            btnSelectTime.setText("Starts at: " + sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    private void generateAndCreateChallenge() {
        if (selectedStartTime < System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Please select a future start time", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedRbId = rgQuestionCount.getCheckedRadioButtonId();
        int count = 10;
        if (checkedRbId == R.id.rb15) count = 15;
        else if (checkedRbId == R.id.rb20) count = 20;
        else if (checkedRbId == R.id.rb30) count = 30;
        else if (checkedRbId == R.id.rb40) count = 40;

        int checkedChipId = cgDifficulty.getCheckedChipId();
        String difficulty = "Easy";
        if (checkedChipId == R.id.chipMedium) difficulty = "Medium";
        else if (checkedChipId == R.id.chipHard) difficulty = "Hard";
        else if (checkedChipId == R.id.chipMixed) difficulty = "Mixed";

        final int finalCount = count;
        final String finalDifficulty = difficulty;
        final int durationMins = (int) sliderDuration.getValue();

        btnCreateChallenge.setEnabled(false);
        
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setMessage("Generating the shared quiz materials...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        new Thread(() -> {
            Deck deck = AppDatabase.getInstance(requireContext()).deckDao().getDeckById(deckId);
            if (deck == null) {
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    btnCreateChallenge.setEnabled(true);
                });
                return;
            }

            new GeminiApiService().generateDeck(deck.getNotes(), 0, finalCount, new GeminiApiService.ApiCallback() {
                @Override
                public void onSuccess(List<Flashcard> flashcards, List<QuizQuestion> questions) {
                    String quizJson = new Gson().toJson(questions);
                    String code = generateChallengeCode();
                    
                    SupabaseRepo.createChallenge(deckId, deckTitle, deck.getNotes(), finalCount, finalDifficulty, selectedStartTime, durationMins, code, quizJson, challengeId -> {
                        if (getActivity() == null) return;
                        loadingDialog.dismiss();
                        if (challengeId != null) {
                            Bundle args = new Bundle();
                            args.putInt("challengeId", challengeId);
                            args.putString("challengeCode", code);
                            Navigation.findNavController(requireView()).navigate(R.id.challengeWaitingRoomFragment, args);
                        } else {
                            btnCreateChallenge.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to create challenge", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    if (getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        btnCreateChallenge.setEnabled(true);
                        Toast.makeText(getContext(), "AI Error: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }).start();
    }

    private String generateChallengeCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
}
