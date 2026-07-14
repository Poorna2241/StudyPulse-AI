package com.yourgroup.studypulseai.network;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizQuestion;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GeminiApiService {
    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();

    public interface ApiCallback {
        void onSuccess(List<Flashcard> flashcards, List<QuizQuestion> questions);
        void onError(String message);
    }

    public GeminiApiService() {
        GenerativeModel gm = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.0-flash");
        this.model = GenerativeModelFutures.from(gm);
    }

    public void generateDeck(String notes, int flashcardCount, int quizCount, ApiCallback callback) {
        String prompt = "You are an expert study assistant. Based ONLY on the study notes provided below, " +
                "generate EXACTLY " + flashcardCount + " flashcards and EXACTLY " + quizCount +
                " quiz questions. \n\n" +
                "GUIDELINES:\n" +
                "1. If notes are brief, create application-based, scenario, or reasoning questions to reach the target count.\n" +
                "2. Stay strictly faithful to the notes. Do not introduce new topics.\n" +
                "3. If a count is 0, return an empty array for that field.\n\n" +
                "OUTPUT FORMAT:\n" +
                "Respond ONLY with a single valid JSON object in this exact format:\n" +
                "{\n" +
                "  \"flashcards\": [{\"question\": \"...\", \"answer\": \"...\"}],\n" +
                "  \"quiz\": [{\"question\": \"...\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correct_index\": 0}]\n" +
                "}\n\n" +
                "NOTES:\n" + notes;

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        AtomicBoolean alreadyResolved = new AtomicBoolean(false);
        timeoutExecutor.schedule(() -> {
            if (alreadyResolved.compareAndSet(false, true)) {
                response.cancel(true);
                callback.onError("Request timed out. Check your internet connection and try again.");
            }
        }, 60, TimeUnit.SECONDS); // Increased to 60s for potentially larger requests

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (!alreadyResolved.compareAndSet(false, true)) return;

                try {
                    String text = result.getText();
                    if (text == null) {
                        callback.onError("Gemini returned empty response");
                        return;
                    }

                    if (text.contains("```json")) {
                        text = text.substring(text.indexOf("```json") + 7);
                        text = text.substring(0, text.lastIndexOf("```"));
                    } else if (text.contains("```")) {
                        text = text.substring(text.indexOf("```") + 3);
                        text = text.substring(0, text.lastIndexOf("```"));
                    }

                    JSONObject data = new JSONObject(text.trim());

                    List<Flashcard> flashcards = new ArrayList<>();
                    if (data.has("flashcards")) {
                        flashcards = new Gson().fromJson(
                                data.getJSONArray("flashcards").toString(),
                                new TypeToken<List<Flashcard>>(){}.getType());
                    }

                    List<QuizQuestion> questions = new ArrayList<>();
                    if (data.has("quiz")) {
                        questions = new Gson().fromJson(
                                data.getJSONArray("quiz").toString(),
                                new TypeToken<List<QuizQuestion>>(){}.getType());
                    }

                    callback.onSuccess(flashcards, questions);
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage() + "\nResponse: " + result.getText());
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull Throwable t) {
                if (!alreadyResolved.compareAndSet(false, true)) return;
                callback.onError("Gemini API error: " + t.getMessage());
            }
        }, executor);
    }
}
