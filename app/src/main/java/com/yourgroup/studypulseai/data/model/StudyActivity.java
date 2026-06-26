package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_activity")
public class StudyActivity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long dateMillis; // Start of the day in millis
    private int actionCount; // Number of cards flipped, etc.
    private int durationMinutes;

    public StudyActivity(long dateMillis, int actionCount, int durationMinutes) {
        this.dateMillis = dateMillis;
        this.actionCount = actionCount;
        this.durationMinutes = durationMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getDateMillis() { return dateMillis; }
    public void setDateMillis(long dateMillis) { this.dateMillis = dateMillis; }
    public int getActionCount() { return actionCount; }
    public void setActionCount(int actionCount) { this.actionCount = actionCount; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}