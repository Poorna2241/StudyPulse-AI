package com.yourgroup.studypulseai.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizQuestion;

@Database(entities = {Deck.class, Flashcard.class, QuizQuestion.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract DeckDao deckDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "studypulse_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}