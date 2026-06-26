package com.yourgroup.studypulseai.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private TextView tvGreeting;
    private RecyclerView rvDecks;
    private View emptyState;
    private DeckAdapter adapter;
    private SearchView searchViewDecks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        rvDecks = view.findViewById(R.id.rvDecks);
        emptyState = view.findViewById(R.id.emptyState);
        searchViewDecks = view.findViewById(R.id.searchViewDecks);

        setupRecyclerView();
        updateUserGreeting();
        setupSearchView();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new DeckAdapter(new DeckAdapter.OnDeckClickListener() {
            @Override public void onStudyClick(Deck deck) { /* Navigate to Study */ }
            @Override public void onQuizClick(Deck deck) { /* Navigate to Quiz */ }
        });
        rvDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDecks.setAdapter(adapter);

        // Dummy data for visual verification
        List<Deck> dummyDecks = new ArrayList<>();
        dummyDecks.add(new Deck("Photosynthesis", "c1"));
        dummyDecks.add(new Deck("Quantum Physics", "c2"));
        dummyDecks.add(new Deck("Ancient History", "c3"));
        adapter.setDecks(dummyDecks);

        if (dummyDecks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvDecks.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvDecks.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = getString(R.string.default_user_name);
            }
            tvGreeting.setText(getString(R.string.greeting_template, name));
        }
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
}