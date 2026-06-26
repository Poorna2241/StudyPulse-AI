package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deckId;
    private String question;
    private String answer;

    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}