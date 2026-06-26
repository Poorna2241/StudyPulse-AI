package com.yourgroup.studypulseai.ui.study;

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
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.yourgroup.studypulseai.R;

public class QuizFragment extends Fragment {
    private TextView tvQuestion;
    private RadioGroup radioOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private MaterialButton btnSubmitAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        tvQuestion = view.findViewById(R.id.tvQuestion);
        radioOptions = view.findViewById(R.id.radioOptions);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        btnSubmitAnswer = view.findViewById(R.id.btnSubmitAnswer);

        // Dummy question
        tvQuestion.setText("Which organelle is known as the powerhouse of the cell?");
        optionA.setText("Nucleus");
        optionB.setText("Mitochondria");
        optionC.setText("Ribosome");
        optionD.setText("Golgi Apparatus");

        btnSubmitAnswer.setOnClickListener(v -> {
            int checkedId = radioOptions.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.optionB) {
                Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
                com.yourgroup.studypulseai.util.ProgressManager.recordQuizResult(requireContext(), 0, 100);
            } else {
                Toast.makeText(getContext(), "Incorrect. Try again!", Toast.LENGTH_SHORT).show();
                com.yourgroup.studypulseai.util.ProgressManager.recordQuizResult(requireContext(), 0, 0);
            }
        });

        return view;
    }
}