package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.yourgroup.studypulseai.data.db.Converters;
import java.util.List;

@Entity(tableName = "quiz_questions")
@TypeConverters(Converters.class)
public class QuizQuestion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deckId;
    private String question;
    private List<String> options;
    private int correctIndex;

    public QuizQuestion(String question, List<String> options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}