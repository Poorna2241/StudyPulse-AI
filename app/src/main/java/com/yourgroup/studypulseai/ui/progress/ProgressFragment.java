package com.yourgroup.studypulseai.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yourgroup.studypulseai.R;

public class ProgressFragment extends Fragment {
    private TextView tvCardsStudied, tvAvgScore;
    private RecyclerView rvQuizHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        tvCardsStudied = view.findViewById(R.id.tvCardsStudied);
        tvAvgScore = view.findViewById(R.id.tvAvgScore);
        rvQuizHistory = view.findViewById(R.id.rvQuizHistory);

        rvQuizHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dummy data
        tvCardsStudied.setText("42");
        tvAvgScore.setText("85%");

        return view;
    }
}