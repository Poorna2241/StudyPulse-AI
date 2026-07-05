package com.yourgroup.studypulseai.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {
    private List<Deck> decks = new ArrayList<>();
    private List<Deck> decksFull = new ArrayList<>();
    private final OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onStudyClick(Deck deck);
        void onQuizClick(Deck deck);
        void onDeleteClick(Deck deck);
    }

    public DeckAdapter(OnDeckClickListener listener) {
        this.listener = listener;
    }

    public void setDecks(List<Deck> decks) {
        this.decks = new ArrayList<>(decks);
        this.decksFull = new ArrayList<>(decks);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        decks.clear();
        if (text.isEmpty()) {
            decks.addAll(decksFull);
        } else {
            text = text.toLowerCase();
            for (Deck item : decksFull) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    decks.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = decks.get(position);
        holder.tvTitle.setText(deck.getTitle());
        holder.tvCount.setText("0 cards"); // Placeholder

        Context context = holder.itemView.getContext();
        int resId = context.getResources().getIdentifier(deck.getBackgroundImage(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivBackground.setImageResource(resId);
        } else {
            holder.ivBackground.setImageResource(R.drawable.c1); // Fallback
        }

        holder.btnStudy.setOnClickListener(v -> listener.onStudyClick(deck));
        holder.btnQuiz.setOnClickListener(v -> listener.onQuizClick(deck));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(deck));
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount;
        ImageView ivBackground;
        View btnStudy, btnQuiz, btnDelete;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDeckTitle);
            tvCount = itemView.findViewById(R.id.tvCardCount);
            ivBackground = itemView.findViewById(R.id.ivDeckBackground);
            btnStudy = itemView.findViewById(R.id.btnStudy);
            btnQuiz = itemView.findViewById(R.id.btnQuiz);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
