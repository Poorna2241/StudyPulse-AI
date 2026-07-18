package com.yourgroup.studypulseai.ui.challenge;

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
import com.yourgroup.studypulseai.network.SupabaseRepo;
import com.yourgroup.studypulseai.network.models.SChallenge;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChallengeHistoryFragment extends Fragment {
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge_history, container, false);
        rvHistory = view.findViewById(R.id.rvChallengeHistory);
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        loadHistory();
        return view;
    }

    private void loadHistory() {
        SupabaseRepo.fetchUserChallenges(challenges -> {
            if (getActivity() == null) return;
            List<SChallenge> sorted = new ArrayList<>(challenges);
            Collections.sort(sorted, (c1, c2) -> Long.compare(c2.getStart_time(), c1.getStart_time()));
            getActivity().runOnUiThread(() -> adapter.setItems(sorted));
        });
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<SChallenge> items = new ArrayList<>();

        public void setItems(List<SChallenge> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SChallenge c = items.get(position);
            holder.tvTitle.setText(c.getDeck_title());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            String date = sdf.format(new Date(c.getStart_time()));
            holder.tvInfo.setText(c.getQuestion_count() + " Qs • " + date);

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("challengeId", c.getId());
                Navigation.findNavController(v).navigate(R.id.challengeLeaderboardFragment, args);
            });

            if ("finished".equalsIgnoreCase(c.getStatus())) {
                holder.tvRank.setVisibility(View.VISIBLE);
                holder.tvRank.setText("Closed");
                holder.tvRank.setTextColor(getResources().getColor(R.color.text_secondary, null));
            } else {
                holder.tvRank.setVisibility(View.VISIBLE);
                holder.tvRank.setText("Active");
                holder.tvRank.setTextColor(getResources().getColor(R.color.correct, null));
            }
            holder.tvScore.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvInfo, tvRank, tvScore;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvDeckTitle);
                tvInfo = itemView.findViewById(R.id.tvChallengeInfo);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvScore = itemView.findViewById(R.id.tvScore);
            }
        }
    }
}
