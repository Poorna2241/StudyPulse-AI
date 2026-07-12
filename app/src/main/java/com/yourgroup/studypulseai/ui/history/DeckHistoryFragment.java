package com.yourgroup.studypulseai.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.QuizResult;
import java.util.List;

public class DeckHistoryFragment extends Fragment {
    private RecyclerView rvQuizHistory;
    private TextView tvEmptyHistory, tvHistoryTitle;
    private QuizAttemptAdapter adapter;
    private int deckId = -1;
    private String deckTitle = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deck_history, container, false);

        if (getArguments() != null) {
            deckId = getArguments().getInt("deckId", -1);
            deckTitle = getArguments().getString("deckTitle", "Deck");
        }

        rvQuizHistory = view.findViewById(R.id.rvQuizHistory);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);

        tvHistoryTitle.setText(deckTitle + " - History");

        setupRecyclerView();
        loadHistory();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new QuizAttemptAdapter(result -> {
            Bundle args = new Bundle();
            args.putInt("resultId", result.getId());
            args.putString("deckTitle", deckTitle);
            Navigation.findNavController(requireView()).navigate(R.id.action_deckHistoryFragment_to_quizReviewFragment, args);
        });
        rvQuizHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        new Thread(() -> {
            List<QuizResult> history = AppDatabase.getInstance(requireContext())
                    .deckDao().getQuizResultsForDeck(deckId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (history == null || history.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        rvQuizHistory.setVisibility(View.GONE);
                    } else {
                        tvEmptyHistory.setVisibility(View.GONE);
                        rvQuizHistory.setVisibility(View.VISIBLE);
                        adapter.setAttempts(history);
                    }
                });
            }
        }).start();
    }
}