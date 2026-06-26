package com.yourgroup.studypulseai.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.yourgroup.studypulseai.data.model.Deck;
import java.util.List;

@Dao
public interface DeckDao {
    @Insert
    long insertDeck(Deck deck);

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    List<Deck> getAllDecks();

    @Query("DELETE FROM decks WHERE id = :deckId")
    void deleteDeck(int deckId);
}