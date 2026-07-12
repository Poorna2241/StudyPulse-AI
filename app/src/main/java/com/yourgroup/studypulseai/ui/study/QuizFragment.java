package com.yourgroup.studypulseai.ui.study;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.util.ProgressManager;

import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment {
    private TextView tvQuestion, tvQuizCounter;
    private RadioGroup radioOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private MaterialButton btnSubmitAnswer;
    private LinearProgressIndicator quizProgress;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int deckId = -1;
    private String deckTitle = "";
    private boolean isAnswerSubmitted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        if (getArguments() != null) {
            deckId = getArguments().getInt("deckId", -1);
            deckTitle = getArguments().getString("deckTitle", "Deck");
        }

        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvQuizCounter = view.findViewById(R.id.tvQuizCounter);
        radioOptions = view.findViewById(R.id.radioOptions);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        btnSubmitAnswer = view.findViewById(R.id.btnSubmitAnswer);
        quizProgress = view.findViewById(R.id.quizProgress);

        btnSubmitAnswer.setOnClickListener(v -> {
            if (quizQuestions.isEmpty()) return;

            if (!isAnswerSubmitted) {
                submitAnswer();
            } else {
                advanceQuiz();
            }
        });

        loadQuizQuestions();

        return view;
    }

    private void loadQuizQuestions() {
        if (deckId == -1) {
            Toast.makeText(getContext(), "Invalid Deck ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<QuizQuestion> loaded = AppDatabase.getInstance(requireContext())
                    .deckDao().getQuizQuestionsByDeck(deckId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    quizQuestions = loaded;
                    if (quizQuestions.isEmpty()) {
                        tvQuestion.setText("No quiz questions available for this deck. Please generate a new deck with questions.");
                        tvQuizCounter.setText("0 / 0");
                        optionA.setVisibility(View.GONE);
                        optionB.setVisibility(View.GONE);
                        optionC.setVisibility(View.GONE);
                        optionD.setVisibility(View.GONE);
                        btnSubmitAnswer.setEnabled(false);
                    } else {
                        currentIndex = 0;
                        score = 0;
                        isAnswerSubmitted = false;
                        showQuestion();
                    }
                });
            }
        }).start();
    }

    private void showQuestion() {
        if (quizQuestions.isEmpty() || currentIndex >= quizQuestions.size()) return;

        QuizQuestion current = quizQuestions.get(currentIndex);
        tvQuestion.setText(current.getQuestion());

        RadioButton[] buttons = {optionA, optionB, optionC, optionD};
        List<String> options = current.getOptions();

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisibility(View.GONE);
            buttons[i].setChecked(false);
            buttons[i].setEnabled(true);
            // Reset text color to default
            buttons[i].setTextColor(getResources().getColor(R.color.text_primary, null));
            buttons[i].setButtonTintList(null); // Reset tints
        }

        for (int i = 0; i < Math.min(options.size(), buttons.length); i++) {
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].setText(options.get(i));
        }

        radioOptions.clearCheck();
        isAnswerSubmitted = false;
        btnSubmitAnswer.setText("Submit Answer");

        tvQuizCounter.setText((currentIndex + 1) + " / " + quizQuestions.size());
        int progress = (int) (((float) currentIndex / quizQuestions.size()) * 100);
        quizProgress.setProgress(progress);
    }

    private void submitAnswer() {
        int checkedId = radioOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        isAnswerSubmitted = true;
        QuizQuestion current = quizQuestions.get(currentIndex);
        int correctIndex = current.getCorrectIndex();

        RadioButton[] buttons = {optionA, optionB, optionC, optionD};
        int selectedIndex = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getId() == checkedId) {
                selectedIndex = i;
                break;
            }
        }

        // Disable all radio buttons
        for (RadioButton button : buttons) {
            button.setEnabled(false);
        }

        // Color validation styling
        if (selectedIndex == correctIndex) {
            buttons[selectedIndex].setTextColor(Color.parseColor("#2ECC71"));
            buttons[selectedIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
            score++;
            Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            if (selectedIndex != -1) {
                buttons[selectedIndex].setTextColor(Color.parseColor("#E74C3C"));
                buttons[selectedIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C")));
            }
            if (correctIndex >= 0 && correctIndex < buttons.length) {
                buttons[correctIndex].setTextColor(Color.parseColor("#2ECC71"));
                buttons[correctIndex].setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
            }
            Toast.makeText(getContext(), "Incorrect", Toast.LENGTH_SHORT).show();
        }

        if (currentIndex == quizQuestions.size() - 1) {
            btnSubmitAnswer.setText("Finish Quiz");
        } else {
            btnSubmitAnswer.setText("Next Question");
        }
    }

    private void advanceQuiz() {
        if (currentIndex < quizQuestions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            // End of quiz
            int scorePercentage = (int) (((float) score / quizQuestions.size()) * 100);
            ProgressManager.recordQuizResult(requireContext(), deckId, scorePercentage);

            quizProgress.setProgress(100);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Quiz Completed!")
                    .setMessage("You scored " + score + " out of " + quizQuestions.size() + " (" + scorePercentage + "%).")
                    .setCancelable(false)
                    .setPositiveButton("Go Home", (dialog, which) -> {
                        Navigation.findNavController(requireView()).popBackStack();
                    })
                    .show();
        }
    }
}