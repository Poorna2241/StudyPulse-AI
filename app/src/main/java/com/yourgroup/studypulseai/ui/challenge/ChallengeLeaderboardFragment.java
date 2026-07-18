package com.yourgroup.studypulseai.ui.challenge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.network.SupabaseRepo;
import com.yourgroup.studypulseai.network.models.SChallengeParticipant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChallengeLeaderboardFragment extends Fragment {
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private int challengeId = -1;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deck_history, container, false);

        if (getArguments() != null) {
            challengeId = getArguments().getInt("challengeId", -1);
        }

        rvLeaderboard = view.findViewById(R.id.rvQuizHistory);
        TextView title = view.findViewById(R.id.tvHistoryTitle);
        title.setText("Challenge Leaderboard");

        adapter = new LeaderboardAdapter();
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(requireView()).navigate(R.id.action_challengeLeaderboardFragment_to_homeFragment);
            }
        });

        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        SupabaseRepo.getChallengeById(challengeId, challenge -> {
            if (getActivity() == null || challenge == null) return;

            long endTime = challenge.getStart_time() + (challenge.getDuration_mins() * 60000L);
            String challengeStatus = challenge.getStatus();

            refreshRunnable = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null || getView() == null) return;
                    
                    long now = System.currentTimeMillis();
                    // Reveal if time is up OR if the challenge is explicitly marked as "finished" (host ended it)
                    boolean revealNow = "finished".equalsIgnoreCase(challengeStatus) || now >= endTime;

                    SupabaseRepo.listenToParticipants(challengeId, participants -> {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (getView() == null) return;
                            TextView titleView = getView().findViewById(R.id.tvHistoryTitle);
                            if (!revealNow) {
                                long remaining = (endTime - now) / 1000;
                                titleView.setText("Waiting for others... (" + Math.max(0, remaining) + "s)");
                                adapter.setParticipants(new ArrayList<>(participants), false);
                                refreshHandler.postDelayed(refreshRunnable, 1000);
                            } else {
                                titleView.setText("Final Leaderboard 🏆");
                                List<SChallengeParticipant> sorted = new ArrayList<>(participants);
                                Collections.sort(sorted, (p1, p2) -> {
                                    if (p1.getScore() != p2.getScore()) {
                                        return Integer.compare(p2.getScore(), p1.getScore());
                                    }
                                    return Long.compare(p1.getCompletion_time_ms(), p2.getCompletion_time_ms());
                                });
                                adapter.setParticipants(sorted, true);
                                if (!"finished".equalsIgnoreCase(challengeStatus)) {
                                    SupabaseRepo.updateChallengeStatus(challengeId, "finished");
                                }
                            }
                        });
                    });
                }
            };
            refreshHandler.post(refreshRunnable);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshHandler.removeCallbacksAndMessages(null);
    }

    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<SChallengeParticipant> items = new ArrayList<>();
        private boolean reveal = false;

        public void setParticipants(List<SChallengeParticipant> participants, boolean reveal) {
            this.items = participants;
            this.reveal = reveal;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge_leaderboard, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SChallengeParticipant p = items.get(position);
            holder.tvName.setText(p.getUser_name());
            
            if (reveal) {
                holder.tvRank.setText(String.valueOf(position + 1));
                holder.tvScore.setText(p.getScore() + "%");
                long mins = (p.getCompletion_time_ms() / 1000) / 60;
                long secs = (p.getCompletion_time_ms() / 1000) % 60;
                holder.tvTime.setText(String.format(Locale.getDefault(), "%dm %ds", mins, secs));
                
                if (position == 0) holder.tvRank.setText("🥇");
                else if (position == 1) holder.tvRank.setText("🥈");
                else if (position == 2) holder.tvRank.setText("🥉");
            } else {
                holder.tvRank.setText("?");
                holder.tvScore.setText("---");
                holder.tvTime.setText("Playing...");
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvScore, tvTime;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvName = itemView.findViewById(R.id.tvPlayerName);
                tvScore = itemView.findViewById(R.id.tvScore);
                tvTime = itemView.findViewById(R.id.tvCompletionTime);
            }
        }
    }
}
