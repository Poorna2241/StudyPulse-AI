package com.yourgroup.studypulseai.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.QuizAttemptQuestion;
import java.util.List;

public class QuizReviewFragment extends Fragment {
    private RecyclerView rvReviewQuestions;
    private TextView tvReviewTitle;
    private ReviewQuestionAdapter adapter;
    private int resultId = -1;
    private String deckTitle = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_review, container, false);

        if (getArguments() != null) {
            resultId = getArguments().getInt("resultId", -1);
            deckTitle = getArguments().getString("deckTitle", "Quiz");
        }

        rvReviewQuestions = view.findViewById(R.id.rvReviewQuestions);
        tvReviewTitle = view.findViewById(R.id.tvReviewTitle);

        tvReviewTitle.setText(deckTitle + " Review");

        setupRecyclerView();
        loadReviewData();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ReviewQuestionAdapter();
        rvReviewQuestions.setAdapter(adapter);
    }

    private void loadReviewData() {
        new Thread(() -> {
            List<QuizAttemptQuestion> questions = AppDatabase.getInstance(requireContext())
                    .deckDao().getQuestionsForAttempt(resultId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setQuestions(questions);
                });
            }
        }).start();
    }
}