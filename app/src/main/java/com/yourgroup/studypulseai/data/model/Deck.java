package com.yourgroup.studypulseai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// This class represents a Deck entity in the Room database. It contains fields for the deck's ID, title, background image, and creation timestamp. The ID is auto-generated, and the createdAt field is set to the current system time when a new Deck is instantiated.
@Entity(tableName = "decks")
public class Deck {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String backgroundImage;
    public String notes; // Source text for AI generation
    public long createdAt;

    public Deck() {
    }

    public Deck(String title, String backgroundImage) {
        this.title = title;
        this.backgroundImage = backgroundImage;
        this.createdAt = System.currentTimeMillis();
    }

    public Deck(String title, String backgroundImage, String notes) {
        this.title = title;
        this.backgroundImage = backgroundImage;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters for the Deck class fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}