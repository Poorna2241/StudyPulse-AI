package com.yourgroup.studypulseai.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.QuizResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizAttemptAdapter extends RecyclerView.Adapter<QuizAttemptAdapter.AttemptViewHolder> {
    private List<QuizResult> attempts = new ArrayList<>();
    private final OnAttemptClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public interface OnAttemptClickListener {
        void onAttemptClick(QuizResult result);
    }

    public QuizAttemptAdapter(OnAttemptClickListener listener) {
        this.listener = listener;
    }

    public void setAttempts(List<QuizResult> attempts) {
        this.attempts = attempts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttemptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_attempt, parent, false);
        return new AttemptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttemptViewHolder holder, int position) {
        QuizResult result = attempts.get(position);
        Date date = new Date(result.getTimestamp());
        holder.tvDate.setText(dateFormat.format(date));
        holder.tvTime.setText(timeFormat.format(date));
        holder.tvScore.setText(result.getScore() + "%");
        holder.itemView.setOnClickListener(v -> listener.onAttemptClick(result));
    }

    @Override
    public int getItemCount() {
        return attempts.size();
    }

    static class AttemptViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvScore;

        public AttemptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvAttemptDate);
            tvTime = itemView.findViewById(R.id.tvAttemptTime);
            tvScore = itemView.findViewById(R.id.tvAttemptScore);
        }
    }
}