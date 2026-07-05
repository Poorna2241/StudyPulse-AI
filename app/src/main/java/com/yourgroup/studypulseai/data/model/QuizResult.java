package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_results")
public class QuizResult {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deckId;
    private int score; // Percentage (0-100)
    private long timestamp;

    public QuizResult(int deckId, int score, long timestamp) {
        this.deckId = deckId;
        this.score = score;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}