package com.yourgroup.studypulseai.ui.challenge;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.network.SupabaseRepo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChallengeQuizFragment extends Fragment {
    private TextView tvQuestion, tvQuizCounter, tvTimer;
    private RadioGroup radioOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private MaterialButton btnSubmitAnswer;
    private LinearProgressIndicator quizProgress;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int challengeId = -1;
    private long startTimeMillis = 0;
    private boolean isAnswerSubmitted = false;
    private int currentCorrectIndex = -1;
    private CountDownTimer challengeTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        if (getArguments() != null) {
            challengeId = getArguments().getInt("challengeId", -1);
        }

        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvQuizCounter = view.findViewById(R.id.tvQuizCounter);
        tvTimer = view.findViewById(R.id.tvChallengeTimer);
        radioOptions = view.findViewById(R.id.radioOptions);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        btnSubmitAnswer = view.findViewById(R.id.btnSubmitAnswer);
        quizProgress = view.findViewById(R.id.quizProgress);

        tvTimer.setVisibility(View.VISIBLE);

        btnSubmitAnswer.setOnClickListener(v -> {
            if (quizQuestions.isEmpty()) return;
            if (!isAnswerSubmitted) submitAnswer();
            else advanceQuiz();
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Leave Challenge Quiz?")
                        .setMessage("You can re-join from the Home screen, but your progress in this attempt will be lost.")
                        .setPositiveButton("Leave", (d, w) -> Navigation.findNavController(requireView()).navigate(R.id.homeFragment))
                        .setNegativeButton("Stay", null)
                        .show();
            }
        });

        loadSharedQuiz();

        return view;
    }

    private void loadSharedQuiz() {
        SupabaseRepo.getChallengeById(challengeId, challenge -> {
            if (getActivity() == null) return;
            if (challenge != null && challenge.getQuiz_json() != null) {
                try {
                    Type listType = new TypeToken<ArrayList<QuizQuestion>>(){}.getType();
                    List<QuizQuestion> questions = new Gson().fromJson(challenge.getQuiz_json(), listType);
                    
                    // SHUFFLE: Ensure everyone has the same questions but in different order
                    Collections.shuffle(questions);
                    
                    getActivity().runOnUiThread(() -> {
                        quizQuestions = questions;
                        currentIndex = 0;
                        score = 0;
                        isAnswerSubmitted = false;
                        startTimeMillis = System.currentTimeMillis();
                        startTimer(challenge.getDuration_mins());
                        showQuestion();
                    });
                } catch (Exception e) {
                    Log.e("QuizShared", "JSON Parse error", e);
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error loading shared quiz", Toast.LENGTH_SHORT).show());
                }
            } else {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Shared quiz not ready yet", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                });
            }
        });
    }

    private void showQuestion() {
        if (quizQuestions.isEmpty() || currentIndex >= quizQuestions.size()) return;

        QuizQuestion current = quizQuestions.get(currentIndex);
        tvQuestion.setText(current.getQuestion());

        RadioButton[] buttons = {optionA, optionB, optionC, optionD};
        List<String> originalOptions = current.getOptions();
        int originalCorrectIndex = current.getCorrectIndex();

        // Shuffling indices for option order
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < originalOptions.size(); i++) indices.add(i);
        Collections.shuffle(indices);

        List<String> shuffledOptions = new ArrayList<>();
        currentCorrectIndex = -1;
        for (int i = 0; i < indices.size(); i++) {
            int oldIndex = indices.get(i);
            shuffledOptions.add(originalOptions.get(oldIndex));
            if (oldIndex == originalCorrectIndex) currentCorrectIndex = i;
        }

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisibility(View.GONE);
            buttons[i].setChecked(false);
            buttons[i].setEnabled(true);
            buttons[i].setTextColor(getResources().getColor(R.color.text_primary, null));
            buttons[i].setButtonTintList(null);
        }

        for (int i = 0; i < Math.min(shuffledOptions.size(), buttons.length); i++) {
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].setText(shuffledOptions.get(i));
        }

        radioOptions.clearCheck();
        isAnswerSubmitted = false;
        btnSubmitAnswer.setText("Submit Answer");
        tvQuizCounter.setText((currentIndex + 1) + " / " + quizQuestions.size());
        quizProgress.setProgress((int) (((float) currentIndex / quizQuestions.size()) * 100));
    }

    private void submitAnswer() {
        int checkedId = radioOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(getContext(), "Select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        isAnswerSubmitted = true;
        RadioButton[] buttons = {optionA, optionB, optionC, optionD};
        int selectedIndex = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getId() == checkedId) {
                selectedIndex = i;
                break;
            }
        }

        for (RadioButton b : buttons) b.setEnabled(false);

        if (selectedIndex == currentCorrectIndex) {
            buttons[selectedIndex].setTextColor(Color.parseColor("#2ECC71"));
            buttons[selectedIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
            score++;
        } else {
            if (selectedIndex != -1) {
                buttons[selectedIndex].setTextColor(Color.parseColor("#E74C3C"));
                buttons[selectedIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C")));
            }
            buttons[currentCorrectIndex].setTextColor(Color.parseColor("#2ECC71"));
            buttons[currentCorrectIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
        }

        if (currentIndex == quizQuestions.size() - 1) btnSubmitAnswer.setText("Finish Challenge");
        else btnSubmitAnswer.setText("Next Question");
    }

    private void startTimer(int durationMins) {
        if (challengeTimer != null) challengeTimer.cancel();
        challengeTimer = new CountDownTimer(durationMins * 60000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
                }
            }

            @Override
            public void onFinish() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (tvTimer != null) tvTimer.setText("00:00");
                    Toast.makeText(getContext(), "Time's up! Submitting results...", Toast.LENGTH_SHORT).show();
                    finishQuiz();
                });
            }
        }.start();
    }

    private void advanceQuiz() {
        if (currentIndex < quizQuestions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        if (challengeTimer != null) challengeTimer.cancel();
        long totalTime = System.currentTimeMillis() - startTimeMillis;
        int finalScorePercentage = (int) (((float) score / (quizQuestions.isEmpty() ? 1 : quizQuestions.size())) * 100);
        SupabaseRepo.updateParticipantResult(challengeId, finalScorePercentage, totalTime);
        navigateToLeaderboard();
    }

    private void navigateToLeaderboard() {
        if (getActivity() == null || getView() == null) return;
        
        Bundle args = new Bundle();
        args.putInt("challengeId", challengeId);
        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_challengeQuizFragment_to_challengeLeaderboardFragment, args);
        } catch (Exception e) {
            Log.e("QuizCrash", "Navigation failed", e);
        }
    }
}
