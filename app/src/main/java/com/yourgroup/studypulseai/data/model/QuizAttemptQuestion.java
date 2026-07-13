package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.yourgroup.studypulseai.data.db.Converters;
import java.util.List;

@Entity(tableName = "quiz_attempt_questions")
@TypeConverters(Converters.class)
public class QuizAttemptQuestion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int resultId; // Foreign key to QuizResult.id
    private String question;
    private List<String> options;
    private int correctIndex;
    private int userSelectedIndex;

    public QuizAttemptQuestion(int resultId, String question, List<String> options, int correctIndex, int userSelectedIndex) {
        this.resultId = resultId;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.userSelectedIndex = userSelectedIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public int getUserSelectedIndex() { return userSelectedIndex; }
    public void setUserSelectedIndex(int userSelectedIndex) { this.userSelectedIndex = userSelectedIndex; }
}