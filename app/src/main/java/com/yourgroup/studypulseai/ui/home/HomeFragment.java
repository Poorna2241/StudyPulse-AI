package com.yourgroup.studypulseai.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import androidx.navigation.Navigation;


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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        rvDecks = view.findViewById(R.id.rvDecks);
        emptyState = view.findViewById(R.id.emptyState);
        searchViewDecks = view.findViewById(R.id.searchViewDecks);

        setupRecyclerView();
        updateUserGreeting();
        setupSearchView();
        
        loadDecks(); // Load from Room

        return view;
    }

    private void setupRecyclerView() {
        adapter = new DeckAdapter(new DeckAdapter.OnDeckClickListener() {
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
            @Override public void onDeleteClick(Deck deck) {
                showDeleteConfirmation(deck);
            }
        });
        rvDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDecks.setAdapter(adapter);
    }

    private void updateUserGreeting() {
        String name = SupabaseAuthHelper.getCurrentUserEmail();
        if (name == null) name = getString(R.string.default_user_name);
        else if (name.contains("@")) name = name.split("@")[0];

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
