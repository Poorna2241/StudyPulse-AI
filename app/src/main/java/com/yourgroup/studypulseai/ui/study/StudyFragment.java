package com.yourgroup.studypulseai.ui.study;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.yourgroup.studypulseai.R;

public class StudyFragment extends Fragment {
    private MaterialCardView flashCard;
    private TextView tvCardLabel, tvCardContent;
    private boolean isShowingQuestion = true;

    // This would normally come from a ViewModel or arguments
    private String dummyQuestion = "What is Photosynthesis?";
    private String dummyAnswer = "The process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        flashCard = view.findViewById(R.id.flashCard);
        tvCardLabel = view.findViewById(R.id.tvCardLabel);
        tvCardContent = view.findViewById(R.id.tvCardContent);

        tvCardContent.setText(dummyQuestion);

        flashCard.setOnClickListener(v -> {
            flipCard();
            com.yourgroup.studypulseai.util.ProgressManager.recordAction(requireContext());
        });

        return view;
    }

    private void flipCard() {
        flashCard.animate()
            .scaleX(0f)
            .setDuration(150)
            .withEndAction(() -> {
                isShowingQuestion = !isShowingQuestion;
                if (isShowingQuestion) {
                    tvCardLabel.setText("QUESTION");
                    tvCardContent.setText(dummyQuestion);
                    flashCard.setCardBackgroundColor(getResources().getColor(R.color.primary, null));
                } else {
                    tvCardLabel.setText("ANSWER");
                    tvCardContent.setText(dummyAnswer);
                    flashCard.setCardBackgroundColor(getResources().getColor(R.color.accent, null));
                }
                flashCard.animate().scaleX(1f).setDuration(150).start();
            }).start();
    }
}