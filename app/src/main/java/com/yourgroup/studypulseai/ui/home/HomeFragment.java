package com.yourgroup.studypulseai.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import com.yourgroup.studypulseai.network.SupabaseRepo;
import com.yourgroup.studypulseai.network.models.SChallenge;
import androidx.navigation.Navigation;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {
    private TextView tvGreeting, tvSubtitle;
    private RecyclerView rvDecks;
    private View emptyState;
    private DeckAdapter adapter;
    private SearchView searchViewDecks;
    private MaterialButton btnJoinChallenge, btnRejoinChallenge;
    private View btnChallengeHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        rvDecks = view.findViewById(R.id.rvDecks);
        emptyState = view.findViewById(R.id.emptyState);
        searchViewDecks = view.findViewById(R.id.searchViewDecks);
        btnJoinChallenge = view.findViewById(R.id.btnJoinChallenge);
        btnRejoinChallenge = view.findViewById(R.id.btnRejoinChallenge);
        btnChallengeHistory = view.findViewById(R.id.btnChallengeHistory);

        setupRecyclerView();
        updateUserGreeting();
        setupSearchView();
        
        loadDecks(); // Load from Room
        checkActiveChallenge();

        btnJoinChallenge.setOnClickListener(v -> showJoinChallengeDialog());
        btnChallengeHistory.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.challengeHistoryFragment));

        // Long press on Rejoin button to force clear it if stuck
        btnRejoinChallenge.setOnLongClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Stuck Button?")
                .setMessage("Would you like to clear this active room and remove the Re-join button?")
                .setPositiveButton("Clear Button", (d, w) -> {
                    SupabaseRepo.fetchUserChallenges(challenges -> {
                        for (SChallenge c : challenges) {
                            if (!"finished".equalsIgnoreCase(c.getStatus())) {
                                Integer id = c.getId();
                                if (id != null) SupabaseRepo.updateChallengeStatus(id, "finished");
                            }
                        }
                        getActivity().runOnUiThread(() -> {
                            btnRejoinChallenge.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Session cleared", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkActiveChallenge();
    }

    private void checkActiveChallenge() {
        SupabaseRepo.fetchUserChallenges(challenges -> {
            if (getActivity() == null) return;
            
            android.util.Log.d("ChallengeDebug", "Fetched " + challenges.size() + " potential challenges");
            SChallenge active = null;
            long now = System.currentTimeMillis();
            
            for (SChallenge c : challenges) {
                android.util.Log.d("ChallengeDebug", "Checking Challenge ID: " + c.getId() + " Status: " + c.getStatus() + " Start: " + c.getStart_time());
                // AGGRESSIVE AUTO-CLOSE: If challenge is older than 2 hours, force finish it
                long createdAt = c.getStart_time(); 
                if (("waiting".equalsIgnoreCase(c.getStatus()) || "started".equalsIgnoreCase(c.getStatus())) && (now - createdAt > 7200000L)) {
                    android.util.Log.d("ChallengeDebug", "Force-closing expired challenge: " + c.getId());
                    Integer cid = c.getId();
                    if (cid != null) SupabaseRepo.updateChallengeStatus(cid, "finished");
                    continue;
                }

                if ("waiting".equalsIgnoreCase(c.getStatus()) || "started".equalsIgnoreCase(c.getStatus())) {
                    active = c;
                    // break; // Don't break yet, we might need to close others
                }
            }

            final SChallenge finalActive = active;
            getActivity().runOnUiThread(() -> {
                if (finalActive != null) {
                    // Check if user already finished this specific challenge
                    SupabaseRepo.getParticipantStatus(finalActive.getId(), p -> {
                        if (getActivity() == null) return;
                        if (p != null && "finished".equalsIgnoreCase(p.getStatus())) {
                            // User finished early, show "View Results" instead of "Re-join"
                            btnRejoinChallenge.setVisibility(View.VISIBLE);
                            btnRejoinChallenge.setText("View Results");
                            btnRejoinChallenge.setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putInt("challengeId", finalActive.getId());
                                Navigation.findNavController(requireView()).navigate(R.id.challengeLeaderboardFragment, args);
                            });
                        } else {
                            // User hasn't finished, normal re-join logic
                            btnRejoinChallenge.setVisibility(View.VISIBLE);
                            btnRejoinChallenge.setText("Re-join Study Room");
                            btnRejoinChallenge.setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putInt("challengeId", finalActive.getId());
                                args.putString("challengeCode", finalActive.getChallenge_code());
                                
                                if ("waiting".equalsIgnoreCase(finalActive.getStatus())) {
                                    Navigation.findNavController(requireView()).navigate(R.id.challengeWaitingRoomFragment, args);
                                } else {
                                    Navigation.findNavController(requireView()).navigate(R.id.challengeQuizFragment, args);
                                }
                            });
                        }
                    });
                } else {
                    btnRejoinChallenge.setVisibility(View.GONE);
                }
            });
        });
    }

    private void setupRecyclerView() {
        adapter = new DeckAdapter(new DeckAdapter.OnDeckClickListener() {
            @Override public void onDeckClick(Deck deck) {
                Bundle args = new Bundle();
                args.putInt("deckId", deck.getId());
                args.putString("deckTitle", deck.getTitle());
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_deckHistoryFragment, args);
            }
            @Override public void onStudyClick(Deck deck) {
                Bundle args = new Bundle();
                args.putInt("deckId", deck.getId());
                args.putString("deckTitle", deck.getTitle());
                Navigation.findNavController(requireView()).navigate(R.id.studyFragment, args);
            }
            @Override public void onQuizClick(Deck deck) {
                Bundle args = new Bundle();
                args.putInt("deckId", deck.getId());
                args.putString("deckTitle", deck.getTitle());
                Navigation.findNavController(requireView()).navigate(R.id.quizFragment, args);
            }
            @Override public void onChallengeClick(Deck deck) {
                Bundle args = new Bundle();
                args.putInt("deckId", deck.getId());
                args.putString("deckTitle", deck.getTitle());
                Navigation.findNavController(requireView()).navigate(R.id.createChallengeFragment, args);
            }
            @Override public void onDeleteClick(Deck deck) {
                showDeleteConfirmation(deck);
            }
        });
        rvDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDecks.setAdapter(adapter);
    }

    private void showJoinChallengeDialog() {
        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        inputLayout.setPadding(40, 20, 40, 0);
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setHint("Enter 6-digit code");
        inputLayout.addView(editText);

        new AlertDialog.Builder(requireContext())
                .setTitle("Join Study Challenge")
                .setView(inputLayout)
                .setPositiveButton("Join", (dialog, which) -> {
                    if (editText.getText() != null) {
                        String code = editText.getText().toString().toUpperCase().trim();
                        if (code.length() == 6) {
                            joinChallengeByCode(code);
                        } else {
                            Toast.makeText(getContext(), "Invalid code", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void joinChallengeByCode(String code) {
        SupabaseRepo.getChallengeByCode(code, challenge -> {
            if (getActivity() == null) return;
            if (challenge != null) {
                String name = SupabaseAuthHelper.getCurrentUserName();
                String userName = (name != null) ? name : "Player";
                
                Integer cId = challenge.getId();
                if (cId == null) return;
                
                SupabaseRepo.joinChallenge(cId, userName, success -> {
                    if (success) {
                        Bundle args = new Bundle();
                        args.putInt("challengeId", cId);
                        args.putString("challengeCode", code);
                        Navigation.findNavController(requireView()).navigate(R.id.challengeWaitingRoomFragment, args);
                    } else {
                        Toast.makeText(getContext(), "Failed to join room", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Challenge room not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserGreeting() {
        String name = SupabaseAuthHelper.getCurrentUserName();
        if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name)) {
            name = SupabaseAuthHelper.getCurrentUserEmail();
            if (name == null) {
                name = getString(R.string.default_user_name);
            } else if (name.contains("@")) {
                name = name.split("@")[0];
            }
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        String subtitle;

        if (hour >= 5 && hour < 12) {
            greeting = getString(R.string.greeting_morning, name);
            subtitle = getString(R.string.subtitle_morning);
        } else if (hour >= 12 && hour < 17) {
            greeting = getString(R.string.greeting_afternoon, name);
            subtitle = getString(R.string.subtitle_afternoon);
        } else if (hour >= 17 && hour < 21) {
            greeting = getString(R.string.greeting_evening, name);
            subtitle = getString(R.string.subtitle_evening);
        } else {
            // Random motivational greeting for night or general use
            int random = new Random().nextInt(3) + 1;
            int greetingId = getResources().getIdentifier("greeting_motivational_" + random, "string", requireContext().getPackageName());
            int subtitleId = getResources().getIdentifier("subtitle_motivational_" + random, "string", requireContext().getPackageName());
            greeting = getString(greetingId, name);
            subtitle = getString(subtitleId);
        }

        tvGreeting.setText(greeting);
        tvSubtitle.setText(subtitle);
    }

    private void setupSearchView() {
        searchViewDecks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void showDeleteConfirmation(Deck deck) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_delete)
                .setMessage(R.string.confirm_delete_deck)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteDeck(deck))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteDeck(Deck deck) {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext())
                    .deckDao().deleteDeck(deck.getId());
            
            // Reload decks on UI thread
            requireActivity().runOnUiThread(this::loadDecks);
        }).start();
    }

    private void loadDecks() {
        new Thread(() -> {
            List<Deck> decks = AppDatabase.getInstance(requireContext())
                    .deckDao().getAllDecks();
            java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
            for (Deck d : decks) {
                counts.put(d.getId(), AppDatabase.getInstance(requireContext()).deckDao().getFlashcardCount(d.getId()));
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setDecks(decks, counts);
                if (decks.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvDecks.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    rvDecks.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
