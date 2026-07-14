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
        // Firebase AI Logic routes auth through your Firebase project config
        // (google-services.json), not a raw API key bundled in the app.
        // Make sure Gemini Developer API is enabled for your Firebase project:
        // Firebase Console -> AI -> AI Logic -> Get started.
        GenerativeModel gm = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-3.1-flash-lite");
        this.model = GenerativeModelFutures.from(gm);
    }

    public void generateDeck(String notes, int count, ApiCallback callback) {
        String prompt = "You are an expert study assistant. Given the study notes below, generate exactly " + count + " flashcards and exactly " + count + " quiz questions.\n\n" +
                "GUIDELINES FOR QUIZ QUESTIONS:\n" +
                "1. If the notes are brief, do not repeat the same facts. Instead, create a mix of:\n" +
                "   - Direct knowledge questions (recall facts from notes).\n" +
                "   - Application-based questions (apply concepts to real-world scenarios).\n" +
                "   - Scenario-based questions (solve a problem using the concepts).\n" +
                "   - Comparison/Relationship questions (how concepts relate to each other).\n" +
                "   - Reasoning questions (why a concept is important or true).\n" +
                "2. Stay strictly faithful to the concepts in the notes. Do not introduce new topics or external academic knowledge beyond realistic examples for the existing concepts.\n" +
                "3. Ensure each question is unique and high-quality.\n\n" +
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

        // Watchdog: if generateContent() never calls back (hung network call,
        // no response from Google's servers, etc.) this guarantees the user
        // sees an error after 45s instead of a spinner that never resolves.
        AtomicBoolean alreadyResolved = new AtomicBoolean(false);
        timeoutExecutor.schedule(() -> {
            if (alreadyResolved.compareAndSet(false, true)) {
                response.cancel(true);
                callback.onError("Request timed out. Check your internet connection and try again.");
            }
        }, 45, TimeUnit.SECONDS);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (!alreadyResolved.compareAndSet(false, true)) return; // timeout already fired

                try {
                    String text = result.getText();
                    if (text == null) {
                        callback.onError("Gemini returned empty response");
                        return;
                    }

                    // Clean the text in case Gemini adds mar kdown backticks
                    if (text.contains("```json")) {
                        text = text.substring(text.indexOf("```json") + 7);
                        text = text.substring(0, text.lastIndexOf("```"));
                    } else if (text.contains("```")) {
                        text = text.substring(text.indexOf("```") + 3);
                        text = text.substring(0, text.lastIndexOf("```"));
                    }

                    JSONObject data = new JSONObject(text.trim());

                    List<Flashcard> flashcards = new Gson().fromJson(
                            data.getJSONArray("flashcards").toString(),
                            new TypeToken<List<Flashcard>>(){}.getType());

                    List<QuizQuestion> questions = new Gson().fromJson(
                            data.getJSONArray("quiz").toString(),
                            new TypeToken<List<QuizQuestion>>(){}.getType());

                    callback.onSuccess(flashcards, questions);
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage() + "\nResponse: " + result.getText());
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull Throwable t) {
                if (!alreadyResolved.compareAndSet(false, true)) return; // timeout already fired
                callback.onError("Gemini API error: " + t.getMessage());
            }
        }, executor);
    }
}