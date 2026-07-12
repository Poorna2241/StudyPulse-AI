package com.yourgroup.studypulseai.util;

import android.content.Context;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.db.DeckDao;
import com.yourgroup.studypulseai.data.model.QuizResult;
import com.yourgroup.studypulseai.data.model.StudyActivity;
import com.yourgroup.studypulseai.network.SupabaseRepo;

import java.util.Calendar;
import android.util.Log;

public class ProgressManager {
    private static final String TAG = "ProgressManager";
    private static long sessionStartTime = 0;

    public static void startSession() {
        sessionStartTime = System.currentTimeMillis();
        Log.e(TAG, "Session started at: " + sessionStartTime);
    }

    public static void endSession(Context context) {
        if (sessionStartTime == 0) return;

        long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime - sessionStartTime;
        // Convert to minutes, minimum 1 minute if session was active
        int elapsedMins = (int) (elapsedMillis / (60 * 1000));
        if (elapsedMillis > 1000 && elapsedMins == 0) elapsedMins = 1;

        final int finalMins = elapsedMins;
        sessionStartTime = 0; // reset

        new Thread(() -> {
            DeckDao dao = AppDatabase.getInstance(context).deckDao();
            long dayStart = getStartOfDayMillis();
            long dayEnd = dayStart + (24 * 60 * 60 * 1000L);

            StudyActivity activity = dao.getActivityForDateRange(dayStart, dayEnd);
            if (activity == null) {
                activity = new StudyActivity(dayStart, 0, finalMins);
                dao.insertStudyActivity(activity);
            } else {
                activity.setDurationMinutes(activity.getDurationMinutes() + finalMins);
                dao.updateStudyActivity(activity);
            }
            Log.e(TAG, "Session ended. Added " + finalMins + " mins. Total today: " + (activity != null ? activity.getDurationMinutes() : finalMins));
        }).start();
    }

    public static void recordAction(Context context) {
        Log.e(TAG, "recordAction called!");
        new Thread(() -> {
            DeckDao dao = AppDatabase.getInstance(context).deckDao();
            long dayStart = getStartOfDayMillis();
            long dayEnd = dayStart + (24 * 60 * 60 * 1000L);
            
            StudyActivity activity = dao.getActivityForDateRange(dayStart, dayEnd);
            if (activity == null) {
                activity = new StudyActivity(dayStart, 1, 0);
                dao.insertStudyActivity(activity);
            } else {
                activity.setActionCount(activity.getActionCount() + 1);
                dao.updateStudyActivity(activity);
                Log.e(TAG, "Updated action count: " + activity.getActionCount());
            }
        }).start();
    }

    public static void recordQuizResult(Context context, int deckId, int score) {
        Log.e(TAG, "recordQuizResult called for deck: " + deckId + " with score: " + score);
        new Thread(() -> {
            DeckDao dao = AppDatabase.getInstance(context).deckDao();
            QuizResult result = new QuizResult(deckId, score, System.currentTimeMillis());
            dao.insertQuizResult(result);
            
            SupabaseRepo.saveQuizResult("Deck-" + deckId, score, 10, success -> {});
            recordAction(context);
        }).start();
    }

    private static long getStartOfDayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}