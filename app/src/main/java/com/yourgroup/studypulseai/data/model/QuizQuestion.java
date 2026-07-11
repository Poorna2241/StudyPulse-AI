package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.yourgroup.studypulseai.data.db.Converters;
import com.google.gson.annotations.SerializedName;
import java.util.List;

// This class represents a QuizQuestion entity in the Room database. It contains fields for the quiz question's ID, associated deck ID, question text, list of options, and the index of the correct option. The ID is auto-generated, and the options are stored as a list of strings.

@Entity(tableName = "quiz_questions")
@TypeConverters(Converters.class)
public class QuizQuestion {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int deckId;
    
    @SerializedName("question")
    public String question;
    
    @SerializedName("options")
    public List<String> options;
    
    @SerializedName("correct_index")
    public int correctIndex;

    public QuizQuestion() {
    }

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
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
}