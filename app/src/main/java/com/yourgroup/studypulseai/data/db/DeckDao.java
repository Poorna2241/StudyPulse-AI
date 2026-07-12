package com.yourgroup.studypulseai.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizResult;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.data.model.StudyActivity;
import java.util.List;

@Dao
public interface DeckDao {
    @Insert
    long insertDeck(Deck deck);

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    List<Deck> getAllDecks();

    @Query("DELETE FROM decks WHERE id = :deckId")
    void deleteDeck(int deckId);

    // Flashcards
    @Insert
    void insertFlashcard(Flashcard flashcard);

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    List<Flashcard> getFlashcardsByDeck(int deckId);

    @Update
    void updateFlashcard(Flashcard flashcard);

    @Query("SELECT COUNT(*) FROM flashcards")
    int getTotalFlashcardCount();

    @Query("SELECT COUNT(*) FROM flashcards WHERE masteryLevel = :level")
    int getFlashcardCountByMastery(int level);

    // Quiz Results
    @Insert
    void insertQuizResult(QuizResult result);

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    List<QuizResult> getAllQuizResults();

    @Query("SELECT COUNT(*) FROM quiz_results")
    int getTotalQuizzesTaken();

    @Query("SELECT * FROM quiz_results WHERE deckId = :deckId ORDER BY timestamp ASC")
    List<QuizResult> getQuizResultsByDeck(int deckId);

    @Query("SELECT * FROM quiz_results WHERE deckId = :deckId ORDER BY timestamp DESC LIMIT 1")
    QuizResult getLatestQuizResultForDeck(int deckId);

    @Query("SELECT AVG(score) FROM quiz_results")
    double getAverageQuizScore();

    // Study Activity
    @Insert
    void insertStudyActivity(StudyActivity activity);

    @Query("SELECT * FROM study_activity WHERE dateMillis >= :since ORDER BY dateMillis ASC")
    List<StudyActivity> getRecentActivity(long since);
    
    @Query("SELECT SUM(durationMinutes) FROM study_activity")
    int getTotalStudyMinutes();

    @Query("SELECT SUM(actionCount) FROM study_activity")
    int getTotalActionCount();

    @Query("SELECT * FROM study_activity WHERE dateMillis = :todayStart LIMIT 1")
    StudyActivity getActivityForDate(long todayStart);

    @Update
    void updateStudyActivity(StudyActivity activity);

    // Deck stats & quiz questions
    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    int getFlashcardCount(int deckId);

    @Insert
    void insertQuizQuestions(List<QuizQuestion> questions);

    @Query("SELECT * FROM quiz_questions WHERE deckId = :deckId")
    List<QuizQuestion> getQuizQuestionsByDeck(int deckId);
}