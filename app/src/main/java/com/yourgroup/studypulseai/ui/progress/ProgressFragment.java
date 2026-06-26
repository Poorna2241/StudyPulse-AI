package com.yourgroup.studypulseai.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.db.DeckDao;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.yourgroup.studypulseai.data.model.Deck;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    private TextView tvStreakValue, tvMasteryValue, tvCardsValue, tvQuizzesValue;
    private LinearLayout llSubjectProgress;
    private Button btnRefresher;
    private DeckDao deckDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        tvStreakValue = view.findViewById(R.id.tvStreakValue);
        tvMasteryValue = view.findViewById(R.id.tvMasteryValue);
        tvCardsValue = view.findViewById(R.id.tvCardsValue);
        tvQuizzesValue = view.findViewById(R.id.tvQuizzesValue);
        llSubjectProgress = view.findViewById(R.id.llSubjectProgress);
        btnRefresher = view.findViewById(R.id.btnRefresher);

        deckDao = AppDatabase.getInstance(requireContext()).deckDao();

        fetchProgressData();

        btnRefresher.setOnClickListener(v -> {
            // Handle refresher quiz navigation
        });

        return view;
    }

    private void fetchProgressData() {
        new Thread(() -> {
            int totalCards = deckDao.getTotalFlashcardCount();
            int masteredCards = deckDao.getFlashcardCountByMastery(2);
            int quizzesTaken = deckDao.getTotalQuizzesTaken();
            int totalActions = deckDao.getTotalActionCount();
            
            // Calculate streak from recent activity
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            java.util.List<com.yourgroup.studypulseai.data.model.StudyActivity> recentActivity = deckDao.getRecentActivity(sevenDaysAgo);
            
            int calculatedStreak = 0;
            // Simplified streak: just count days in the last 7 days that had activity
            if (recentActivity != null) {
                calculatedStreak = recentActivity.size();
            }
            
            final int masteryPercent = (totalCards > 0) ? (masteredCards * 100 / totalCards) : 0;
            final int streak = calculatedStreak;
            final int actions = totalActions;
            
            List<Deck> decks = deckDao.getAllDecks();

            requireActivity().runOnUiThread(() -> {
                tvStreakValue.setText(String.valueOf(streak));
                tvMasteryValue.setText(getString(R.string.mastery_percentage_template, masteryPercent));
                tvCardsValue.setText(String.format(Locale.getDefault(), "%,d", actions));
                tvQuizzesValue.setText(String.valueOf(quizzesTaken));
                
                updateSubjectProgress(decks);
            });
        }).start();
    }

    private void updateSubjectProgress(List<Deck> decks) {
        llSubjectProgress.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        
        for (Deck deck : decks) {
            View itemView = inflater.inflate(R.layout.item_subject_progress, llSubjectProgress, false);
            TextView tvName = itemView.findViewById(R.id.tvSubjectName);
            ProgressBar progressBar = itemView.findViewById(R.id.pbSubjectMastery);
            
            tvName.setText(deck.getTitle());
            // For now, random progress since we don't have deck-specific mastery calculated yet
            progressBar.setProgress(new java.util.Random().nextInt(60) + 40); 
            
            llSubjectProgress.addView(itemView);
        }
    }
}