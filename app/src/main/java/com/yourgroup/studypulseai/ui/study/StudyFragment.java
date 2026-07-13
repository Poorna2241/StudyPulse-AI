package com.yourgroup.studypulseai.ui.study;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.util.ProgressManager;

import java.util.ArrayList;
import java.util.List;

public class StudyFragment extends Fragment {
    private MaterialCardView flashCard;
    private TextView tvCardLabel, tvCardContent, tvCardCounter;
    private LinearProgressIndicator studyProgress;
    private MaterialButton btnPrev, btnNext;
    private MaterialButtonToggleGroup masteryGroup;

    private List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShowingQuestion = true;
    private int deckId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        if (getArguments() != null) {
            deckId = getArguments().getInt("deckId", -1);
        }

        flashCard = view.findViewById(R.id.flashCard);
        tvCardLabel = view.findViewById(R.id.tvCardLabel);
        tvCardContent = view.findViewById(R.id.tvCardContent);
        tvCardCounter = view.findViewById(R.id.tvCardCounter);
        studyProgress = view.findViewById(R.id.studyProgress);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        masteryGroup = view.findViewById(R.id.masteryGroup);

        flashCard.setOnClickListener(v -> {
            if (!flashcards.isEmpty()) {
                android.util.Log.e("StudyFragment", "Card clicked! Recording action...");
                flipCard();
                ProgressManager.recordAction(requireContext());
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                isShowingQuestion = true;
                showCard();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < flashcards.size() - 1) {
                currentIndex++;
                isShowingQuestion = true;
                showCard();
            } else {
                // Finish session
                if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        setupMasteryListeners();
        loadFlashcards();
        
        ProgressManager.startSession();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ProgressManager.endSession(requireContext());
    }

    private void loadFlashcards() {
        if (deckId == -1) {
            Toast.makeText(getContext(), "Invalid Deck ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<Flashcard> loaded = AppDatabase.getInstance(requireContext())
                    .deckDao().getFlashcardsByDeck(deckId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    flashcards = loaded;
                    if (flashcards.isEmpty()) {
                        tvCardContent.setText("No flashcards in this deck.");
                        tvCardCounter.setText("0 / 0");
                        studyProgress.setProgress(0);
                        btnPrev.setEnabled(false);
                        btnNext.setEnabled(false);
                        masteryGroup.setEnabled(false);
                    } else {
                        currentIndex = 0;
                        isShowingQuestion = true;
                        showCard();
                    }
                });
            }
        }).start();
    }

    private void showCard() {
        if (flashcards.isEmpty() || currentIndex >= flashcards.size()) return;

        Flashcard current = flashcards.get(currentIndex);
        tvCardLabel.setText("QUESTION");
        tvCardContent.setText(current.getQuestion());
        flashCard.setCardBackgroundColor(getResources().getColor(R.color.primary, null));

        tvCardCounter.setText((currentIndex + 1) + " / " + flashcards.size());
        int progress = (int) (((float) (currentIndex + 1) / flashcards.size()) * 100);
        studyProgress.setProgress(progress);

        btnPrev.setEnabled(currentIndex > 0);
        
        if (currentIndex == flashcards.size() - 1) {
            btnNext.setText("Finish");
            btnNext.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.correct, null)));
        } else {
            btnNext.setText("Next");
            btnNext.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.parseColor("#8231BC")));
        }
        btnNext.setEnabled(true);

        // Set toggle group state without triggering listener
        masteryGroup.clearChecked();
        int mastery = current.getMasteryLevel();
        if (mastery == 0) {
            masteryGroup.check(R.id.btnStruggling);
        } else if (mastery == 1) {
            masteryGroup.check(R.id.btnLearning);
        } else if (mastery == 2) {
            masteryGroup.check(R.id.btnMastered);
        }
    }

    private void flipCard() {
        if (flashcards.isEmpty()) return;

        Flashcard current = flashcards.get(currentIndex);
        flashCard.animate()
            .scaleX(0f)
            .setDuration(150)
            .withEndAction(() -> {
                isShowingQuestion = !isShowingQuestion;
                if (isShowingQuestion) {
                    tvCardLabel.setText("QUESTION");
                    tvCardContent.setText(current.getQuestion());
                    flashCard.setCardBackgroundColor(getResources().getColor(R.color.primary, null));
                } else {
                    tvCardLabel.setText("ANSWER");
                    tvCardContent.setText(current.getAnswer());
                    flashCard.setCardBackgroundColor(getResources().getColor(R.color.accent, null));
                }
                flashCard.animate().scaleX(1f).setDuration(150).start();
            }).start();
    }

    private void setupMasteryListeners() {
        masteryGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || flashcards.isEmpty()) return;

            int level = 1; // learning
            if (checkedId == R.id.btnStruggling) {
                level = 0;
            } else if (checkedId == R.id.btnLearning) {
                level = 1;
            } else if (checkedId == R.id.btnMastered) {
                level = 2;
            }

            Flashcard current = flashcards.get(currentIndex);
            if (current.getMasteryLevel() != level) {
                current.setMasteryLevel(level);
                new Thread(() -> {
                    AppDatabase.getInstance(requireContext())
                            .deckDao().updateFlashcard(current);
                    ProgressManager.recordAction(requireContext());
                }).start();
            }
        });
    }
}