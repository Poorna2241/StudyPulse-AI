package com.yourgroup.studypulseai.util;

import android.content.Context;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.db.DeckDao;
import com.yourgroup.studypulseai.data.model.QuizResult;
import com.yourgroup.studypulseai.data.model.StudyActivity;
import com.yourgroup.studypulseai.network.SupabaseRepo;

import java.util.Calendar;

public class ProgressManager {
    
    public static void recordAction(Context context) {
        new Thread(() -> {
            DeckDao dao = AppDatabase.getInstance(context).deckDao();
            long todayStart = getStartOfDayMillis();
            
            StudyActivity activity = dao.getActivityForDate(todayStart);
            if (activity == null) {
                activity = new StudyActivity(todayStart, 1, 0);
                dao.insertStudyActivity(activity);
            } else {
                activity.setActionCount(activity.getActionCount() + 1);
                dao.updateStudyActivity(activity);
            }
        }).start();
    }

    public static void recordQuizResult(Context context, int deckId, int score) {
        new Thread(() -> {
            DeckDao dao = AppDatabase.getInstance(context).deckDao();
            QuizResult result = new QuizResult(deckId, score, System.currentTimeMillis());
            dao.insertQuizResult(result);
            
            // Sync to Supabase
            SupabaseRepo.saveQuizResult("Deck-" + deckId, score, 10, success -> {});

            // Also count as an action
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