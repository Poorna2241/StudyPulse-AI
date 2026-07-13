package com.yourgroup.studypulseai.ui.history;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.QuizAttemptQuestion;
import java.util.ArrayList;
import java.util.List;

public class ReviewQuestionAdapter extends RecyclerView.Adapter<ReviewQuestionAdapter.ReviewViewHolder> {
    private List<QuizAttemptQuestion> questions = new ArrayList<>();

    public void setQuestions(List<QuizAttemptQuestion> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_question, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        QuizAttemptQuestion q = questions.get(position);
        holder.tvQuestion.setText((position + 1) + ". " + q.getQuestion());
        
        holder.llOptions.removeAllViews();
        Context context = holder.itemView.getContext();
        
        List<String> options = q.getOptions();
        for (int i = 0; i < options.size(); i++) {
            TextView tv = new TextView(context);
            tv.setText(options.get(i));
            tv.setPadding(16, 8, 16, 8);
            tv.setTextSize(14f);
            
            if (i == q.getCorrectIndex()) {
                tv.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
                tv.setTextColor(Color.parseColor("#2E7D32"));
            }
            
            if (i == q.getUserSelectedIndex()) {
                if (i != q.getCorrectIndex()) {
                    tv.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
                    tv.setTextColor(Color.parseColor("#C62828"));
                }
                tv.setText(tv.getText() + " (Your Answer)");
            }
            
            holder.llOptions.addView(tv);
        }

        if (q.getUserSelectedIndex() == q.getCorrectIndex()) {
            holder.tvStatus.setText("Correct");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("Incorrect");
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvStatus;
        LinearLayout llOptions;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvReviewQuestion);
            tvStatus = itemView.findViewById(R.id.tvReviewStatus);
            llOptions = itemView.findViewById(R.id.llReviewOptions);
        }
    }
}