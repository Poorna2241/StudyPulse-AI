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
import com.google.android.material.slider.Slider;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.data.model.QuizResult;
import com.yourgroup.studypulseai.network.GeminiApiService;
import com.yourgroup.studypulseai.util.ProgressManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizFragment extends Fragment {
    private TextView tvQuestion, tvQuizCounter;
    private RadioGroup radioOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private MaterialButton btnSubmitAnswer;
    private LinearProgressIndicator quizProgress;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private List<Integer> userAnswers = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int deckId = -1;
    private int currentCorrectIndex = -1; // New field to track shuffled correct index
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
        
        ProgressManager.startSession();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ProgressManager.endSession(requireContext());
    }

    private void loadQuizQuestions() {
        if (deckId == -1) {
            Toast.makeText(getContext(), "Invalid Deck ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            
            // Check if there are ANY previous quiz results for this deck
            List<QuizResult> existingResults = db.deckDao().getQuizResultsForDeck(deckId);
            boolean hasTakenQuizBefore = existingResults != null && !existingResults.isEmpty();

            if (!hasTakenQuizBefore) {
                // FIRST TIME: Automatically show "Preparing..." and load the stored quiz
                requireActivity().runOnUiThread(() -> {
                    AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                            .setMessage("Preparing your first quiz...")
                            .setCancelable(false)
                            .create();
                    loadingDialog.show();

                    new Thread(() -> {
                        List<QuizQuestion> loaded = db.deckDao().getQuizQuestionsByDeck(deckId);
                        requireActivity().runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            quizQuestions = loaded;
                            if (quizQuestions.isEmpty()) {
                                // If missing, regenerate with default count
                                regenerateQuiz(db, null, 10);
                            } else {
                                currentIndex = 0;
                                score = 0;
                                userAnswers.clear();
                                isAnswerSubmitted = false;
                                showQuestion();
                            }
                        });
                    }).start();
                });
            } else {
                // SUBSEQUENT TIMES: Show customization dialog first
                requireActivity().runOnUiThread(this::showCustomizationDialog);
            }
        }).start();
    }

    private void showCustomizationDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_customize_quiz, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Slider slider = dialogView.findViewById(R.id.sliderCustomCount);
        TextView tvValue = dialogView.findViewById(R.id.tvCustomCountValue);
        MaterialButton btnStart = dialogView.findViewById(R.id.btnStartCustomQuiz);

        slider.addOnChangeListener((s, value, fromUser) -> tvValue.setText(String.valueOf((int) value)));

        btnStart.setOnClickListener(v -> {
            int requestedCount = (int) slider.getValue();
            dialog.dismiss();
            
            AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                    .setMessage("Regenerating " + requestedCount + " fresh questions for your quiz...")
                    .setCancelable(false)
                    .create();
            loadingDialog.show();
            
            new Thread(() -> regenerateQuiz(AppDatabase.getInstance(requireContext()), loadingDialog, requestedCount)).start();
        });

        dialog.show();
    }

    private void regenerateQuiz(AppDatabase db, AlertDialog loadingDialog, int requestedCount) {
        new Thread(() -> {
            Deck deck = db.deckDao().getDeckById(deckId);
            if (deck == null || deck.getNotes() == null || deck.getNotes().isEmpty()) {
                loadExistingQuestionsSync(db, loadingDialog);
                return;
            }

            // Perform AI regeneration
            // For quiz-only regeneration, we set flashcard count to 0
            new GeminiApiService().generateDeck(deck.getNotes(), 0, requestedCount, new GeminiApiService.ApiCallback() {
                @Override
                public void onSuccess(List<com.yourgroup.studypulseai.data.model.Flashcard> flashcards, List<QuizQuestion> questions) {
                    new Thread(() -> {
                        db.deckDao().deleteQuizQuestionsByDeck(deckId);
                        for (QuizQuestion q : questions) {
                            q.setDeckId(deckId);
                        }
                        db.deckDao().insertQuizQuestions(questions);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (loadingDialog != null) loadingDialog.dismiss();
                                quizQuestions = questions;
                                currentIndex = 0;
                                score = 0;
                                userAnswers.clear();
                                isAnswerSubmitted = false;
                                showQuestion();
                            });
                        }
                    }).start();
                }

                @Override
                public void onError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (loadingDialog != null) loadingDialog.dismiss();
                            Toast.makeText(getContext(), "Regeneration failed. Loading last questions.", Toast.LENGTH_SHORT).show();
                            loadExistingQuestionsSync(db, null);
                        });
                    }
                }
            });
        }).start();
    }

    private void loadExistingQuestionsSync(AppDatabase db, AlertDialog loadingDialog) {
        new Thread(() -> {
            List<QuizQuestion> loaded = db.deckDao().getQuizQuestionsByDeck(deckId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (loadingDialog != null) loadingDialog.dismiss();
                    quizQuestions = loaded;
                    if (quizQuestions.isEmpty()) {
                        tvQuestion.setText("No quiz questions available.");
                        btnSubmitAnswer.setEnabled(false);
                    } else {
                        currentIndex = 0;
                        score = 0;
                        userAnswers.clear();
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
        List<String> originalOptions = current.getOptions();
        int originalCorrectIndex = current.getCorrectIndex();

        // Shuffle options to prevent predictability
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < originalOptions.size(); i++) indices.add(i);
        Collections.shuffle(indices);

        List<String> shuffledOptions = new ArrayList<>();
        currentCorrectIndex = -1;

        for (int i = 0; i < indices.size(); i++) {
            int oldIndex = indices.get(i);
            shuffledOptions.add(originalOptions.get(oldIndex));
            if (oldIndex == originalCorrectIndex) {
                currentCorrectIndex = i;
            }
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
        int correctIndex = currentCorrectIndex; // Use the shuffled correct index

        RadioButton[] buttons = {optionA, optionB, optionC, optionD};
        int selectedIndex = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getId() == checkedId) {
                selectedIndex = i;
                break;
            }
        }
        userAnswers.add(selectedIndex);

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
            ProgressManager.recordQuizResult(requireContext(), deckId, scorePercentage, quizQuestions, userAnswers);

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
