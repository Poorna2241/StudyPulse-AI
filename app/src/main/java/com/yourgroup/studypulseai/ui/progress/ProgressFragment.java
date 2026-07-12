package com.yourgroup.studypulseai.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.db.DeckDao;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.QuizResult;
import com.yourgroup.studypulseai.data.model.StudyActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class ProgressFragment extends Fragment {
    private static final String TAG = "ProgressFragment";
    private TextView tvStreakValue, tvMasteryValue, tvCardsValue, tvQuizzesValue;
    private LinearLayout llSubjectProgress;
    private Button btnRefresher;
    private DeckDao deckDao;

    // Realtime Charts and Views
    private DonutChartView donutChartView;
    private TextView tvDonutMasteryValue;
    private LineChartView lineChartView;
    private Spinner spinnerDeckFilter;

    private View barMon, barTue, barWed, barThu, barFri, barSat, barSun;
    private TextView tvMinsMon, tvMinsTue, tvMinsWed, tvMinsThu, tvMinsFri, tvMinsSat, tvMinsSun;
    private View forecastDay1, forecastDay2, forecastDay3, forecastDay4, forecastDay5, forecastDay6, forecastDay7;

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

        // Chart bindings
        donutChartView = view.findViewById(R.id.donutChartView);
        tvDonutMasteryValue = view.findViewById(R.id.tvDonutMasteryValue);
        lineChartView = view.findViewById(R.id.lineChartView);
        spinnerDeckFilter = view.findViewById(R.id.spinnerDeckFilter);

        // Weekly activity bars
        barMon = view.findViewById(R.id.barMon);
        barTue = view.findViewById(R.id.barTue);
        barWed = view.findViewById(R.id.barWed);
        barThu = view.findViewById(R.id.barThu);
        barFri = view.findViewById(R.id.barFri);
        barSat = view.findViewById(R.id.barSat);
        barSun = view.findViewById(R.id.barSun);

        tvMinsMon = view.findViewById(R.id.tvMinsMon);
        tvMinsTue = view.findViewById(R.id.tvMinsTue);
        tvMinsWed = view.findViewById(R.id.tvMinsWed);
        tvMinsThu = view.findViewById(R.id.tvMinsThu);
        tvMinsFri = view.findViewById(R.id.tvMinsFri);
        tvMinsSat = view.findViewById(R.id.tvMinsSat);
        tvMinsSun = view.findViewById(R.id.tvMinsSun);

        // Setup click listeners for bars to show minutes
        setupBarClickListeners();

        // Forecast days
        forecastDay1 = view.findViewById(R.id.forecastDay1);
        forecastDay2 = view.findViewById(R.id.forecastDay2);
        forecastDay3 = view.findViewById(R.id.forecastDay3);
        forecastDay4 = view.findViewById(R.id.forecastDay4);
        forecastDay5 = view.findViewById(R.id.forecastDay5);
        forecastDay6 = view.findViewById(R.id.forecastDay6);
        forecastDay7 = view.findViewById(R.id.forecastDay7);

        deckDao = AppDatabase.getInstance(requireContext()).deckDao();

        btnRefresher.setOnClickListener(v -> {
            // Handle refresher quiz navigation
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load real-time progress data whenever the fragment is resumed
        fetchProgressData();
    }

    private void fetchProgressData() {
        new Thread(() -> {
            if (getContext() == null) return;

            int totalCards = deckDao.getTotalFlashcardCount();
            int masteredCards = deckDao.getFlashcardCountByMastery(2);
            int learningCards = deckDao.getFlashcardCountByMastery(1);
            int strugglingCards = deckDao.getFlashcardCountByMastery(0);

            int quizzesTaken = deckDao.getTotalQuizzesTaken();
            int totalActions = deckDao.getTotalActionCount();
            
            // Calculate streak from recent activity
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            List<StudyActivity> recentActivity = deckDao.getRecentActivity(sevenDaysAgo);
            Log.d(TAG, "Recent activity count found: " + (recentActivity != null ? recentActivity.size() : 0));
            
            int calculatedStreak = 0;
            if (recentActivity != null) {
                calculatedStreak = recentActivity.size();
            }
            
            final int masteryPercent = (totalCards > 0) ? (masteredCards * 100 / totalCards) : 0;
            final int streak = calculatedStreak;
            final int actions = totalActions;

            // Compute weekly study minutes
            int[] dailyMinutes = new int[7];
            Calendar cal = Calendar.getInstance();
            if (recentActivity != null) {
                for (StudyActivity activity : recentActivity) {
                    cal.setTimeInMillis(activity.getDateMillis());
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    int index = (dayOfWeek + 5) % 7; // map so Mon = 0, Tue = 1, ... Sun = 6
                    dailyMinutes[index] += activity.getDurationMinutes();
                    Log.d(TAG, "Mapped activity date " + activity.getDateMillis() + " to day index " + index + " with " + activity.getDurationMinutes() + " mins");
                }
            }

            // Compute forecast
            int[] forecastedCount = new int[7];
            for (int day = 0; day < 7; day++) {
                forecastedCount[day] += strugglingCards;
                if (day % 2 == 0) {
                    forecastedCount[day] += learningCards / 2;
                } else {
                    forecastedCount[day] += (learningCards + 1) / 2;
                }
                if (day % 5 == 0) {
                    forecastedCount[day] += masteredCards / 5;
                }
            }

            // Fetch recent quiz scores for "All" initial view
            List<QuizResult> results = deckDao.getAllQuizResults();
            List<Integer> initialQuizScores = new ArrayList<>();
            if (results != null) {
                int limit = Math.min(results.size(), 10);
                for (int i = 0; i < limit; i++) {
                    initialQuizScores.add(results.get(i).getScore());
                }
                Collections.reverse(initialQuizScores); // Chronological order
            }
            
            List<Deck> decksList = deckDao.getAllDecks();
            List<String> deckTitles = new ArrayList<>();
            deckTitles.add("All Decks");
            for (Deck d : decksList) {
                deckTitles.add(d.getTitle());
            }

            java.util.Map<Integer, Integer> masteryMap = new java.util.HashMap<>();
            final List<Deck> finalDecksList;
            if (decksList.isEmpty()) {
                Deck placeholderDeck = new Deck("Computer Science", "c1");
                placeholderDeck.setId(-999);
                finalDecksList = new java.util.ArrayList<>();
                finalDecksList.add(placeholderDeck);
                masteryMap.put(-999, 75);
            } else {
                finalDecksList = decksList;
                for (Deck deck : decksList) {
                    List<com.yourgroup.studypulseai.data.model.Flashcard> cards = deckDao.getFlashcardsByDeck(deck.getId());
                    int deckMastery = 0;
                    if (cards != null && !cards.isEmpty()) {
                        int sumMastery = 0;
                        for (com.yourgroup.studypulseai.data.model.Flashcard card : cards) {
                            sumMastery += card.getMasteryLevel();
                        }
                        deckMastery = (sumMastery * 100) / (cards.size() * 2);
                    }
                    masteryMap.put(deck.getId(), deckMastery);
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (getView() == null) return;

                tvStreakValue.setText(String.valueOf(streak));
                tvMasteryValue.setText(getString(R.string.mastery_percentage_template, masteryPercent));
                tvCardsValue.setText(String.format(Locale.getDefault(), "%,d", actions));
                tvQuizzesValue.setText(String.valueOf(quizzesTaken));

                // Feed real-time data to Donut and Line charts
                donutChartView.setData(strugglingCards, learningCards, masteredCards);
                tvDonutMasteryValue.setText(getString(R.string.mastery_percentage_template, masteryPercent));
                lineChartView.setScores(initialQuizScores);

                // Setup Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, deckTitles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDeckFilter.setAdapter(adapter);

                spinnerDeckFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        new Thread(() -> {
                            List<Integer> filteredScores = new ArrayList<>();
                            List<String> filteredLabels = new ArrayList<>();

                            if (position == 0) {
                                // All Decks - Show Latest Result per Deck
                                List<Deck> allDecks = deckDao.getAllDecks();
                                for (Deck d : allDecks) {
                                    QuizResult latest = deckDao.getLatestQuizResultForDeck(d.getId());
                                    if (latest != null) {
                                        filteredScores.add(latest.getScore());
                                        filteredLabels.add(d.getTitle());
                                    }
                                }
                            } else {
                                // Specific Deck - Show all attempts for this deck
                                Deck selected = decksList.get(position - 1);
                                List<QuizResult> results = deckDao.getQuizResultsByDeck(selected.getId());
                                if (results != null) {
                                    for (int i = 0; i < results.size(); i++) {
                                        filteredScores.add(results.get(i).getScore());
                                        filteredLabels.add("A" + (i + 1)); // Attempt 1, 2, 3...
                                    }
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    lineChartView.setScores(filteredScores, filteredLabels);
                                    // Hide the static axis label container if we have specific labels
                                    View labelsContainer = getView().findViewById(R.id.llChartLabels);
                                    if (labelsContainer != null) {
                                        labelsContainer.setVisibility(filteredLabels.isEmpty() ? View.VISIBLE : View.GONE);
                                    }
                                });
                            }
                        }).start();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });

                // Update Weekly Activity Bar Heights
                View[] bars = {barMon, barTue, barWed, barThu, barFri, barSat, barSun};
                TextView[] minLabels = {tvMinsMon, tvMinsTue, tvMinsWed, tvMinsThu, tvMinsFri, tvMinsSat, tvMinsSun};
                
                for (int i = 0; i < 7; i++) {
                    updateBarWeight(bars[i], dailyMinutes[i]);
                    final int mins = dailyMinutes[i];
                    final TextView label = minLabels[i];
                    requireActivity().runOnUiThread(() -> label.setText(mins + "m"));
                }

                // Update forecast heatmap
                updateForecastView(forecastDay1, forecastedCount[0]);
                updateForecastView(forecastDay2, forecastedCount[1]);
                updateForecastView(forecastDay3, forecastedCount[2]);
                updateForecastView(forecastDay4, forecastedCount[3]);
                updateForecastView(forecastDay5, forecastedCount[4]);
                updateForecastView(forecastDay6, forecastedCount[5]);
                updateForecastView(forecastDay7, forecastedCount[6]);
                
                updateSubjectProgress(finalDecksList, masteryMap);
            });
        }).start();
    }

    private void setupBarClickListeners() {
        View[] bars = {barMon, barTue, barWed, barThu, barFri, barSat, barSun};
        TextView[] labels = {tvMinsMon, tvMinsTue, tvMinsWed, tvMinsThu, tvMinsFri, tvMinsSat, tvMinsSun};

        for (int i = 0; i < bars.length; i++) {
            final int index = i;
            bars[i].setOnClickListener(v -> {
                // Toggle visibility
                boolean isVisible = labels[index].getVisibility() == View.VISIBLE;
                
                // Hide all first for a clean look (only show one at a time)
                for (TextView label : labels) {
                    label.setVisibility(View.INVISIBLE);
                }
                
                // If it was hidden, show it now
                if (!isVisible) {
                    labels[index].setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void updateBarWeight(View bar, int minutes) {
        if (bar == null) return;
        
        bar.post(() -> {
            View parent = (View) bar.getParent();
            if (parent == null) return;
            
            int parentHeight = parent.getHeight();
            if (parentHeight <= 0) return;

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
            
            // Max 240 minutes (4 hours) = 100% height
            // Min weight 0.05 to show a tiny bar even for 0 mins
            float ratio = Math.min(1.0f, (float) Math.max(0, minutes) / 240f);
            if (minutes == 0) ratio = 0.05f; 

            params.height = (int) (parentHeight * ratio);
            bar.setLayoutParams(params);
            bar.setAlpha(minutes > 0 ? 1.0f : 0.3f);
            
            Log.d(TAG, "Set bar height to " + params.height + " px for " + minutes + " mins (ratio: " + ratio + ")");
        });
    }

    private void updateForecastView(View view, int count) {
        if (view == null) return;
        if (count == 0) {
            view.setBackgroundColor(0xFFECEFF1);
            view.setAlpha(0.2f);
        } else if (count < 5) {
            view.setBackgroundColor(0xFFE3F2FD);
            view.setAlpha(0.6f);
        } else if (count < 15) {
            view.setBackgroundColor(0xFF81C784);
            view.setAlpha(0.8f);
        } else {
            view.setBackgroundColor(0xFF4CAF50);
            view.setAlpha(1.0f);
        }
    }

    private void updateSubjectProgress(List<Deck> decks, java.util.Map<Integer, Integer> masteryMap) {
        llSubjectProgress.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        
        for (Deck deck : decks) {
            View itemView = inflater.inflate(R.layout.item_subject_progress, llSubjectProgress, false);
            TextView tvName = itemView.findViewById(R.id.tvSubjectName);
            ProgressBar progressBar = itemView.findViewById(R.id.pbSubjectMastery);
            
            tvName.setText(deck.getTitle());
            int progress = masteryMap.containsKey(deck.getId()) ? masteryMap.get(deck.getId()) : 0;
            progressBar.setProgress(progress); 
            
            llSubjectProgress.addView(itemView);
        }
    }
}