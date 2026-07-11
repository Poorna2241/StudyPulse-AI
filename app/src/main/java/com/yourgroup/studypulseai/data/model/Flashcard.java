package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// This class represents a Flashcard entity in the Room database. It contains fields for the flashcard's ID, associated deck ID, question, answer, and mastery level. The ID is auto-generated, and the mastery level is initialized to 1 (Learning) by default.
@Entity(tableName = "flashcards")
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deckId;
    private String question;
    private String answer;
    
    // 0: Struggling, 1: Learning, 2: Mastered
    private int masteryLevel = 1; 

    public Flashcard() {
    }

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
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
    
    public int getMasteryLevel() { return masteryLevel; }
    public void setMasteryLevel(int masteryLevel) { this.masteryLevel = masteryLevel; }
}