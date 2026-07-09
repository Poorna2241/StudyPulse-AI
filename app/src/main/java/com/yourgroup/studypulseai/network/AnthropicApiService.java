package com.yourgroup.studypulseai.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yourgroup.studypulseai.BuildConfig;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizQuestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnthropicApiService {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY = BuildConfig.ANTHROPIC_API_KEY;
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS).build();

    public interface ApiCallback {
        void onSuccess(List<Flashcard> flashcards, List<QuizQuestion> questions);
        void onError(String message);
    }

    public void generateDeck(String notes, int count, ApiCallback callback) {
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("null")) {
            callback.onError("Anthropic API Key is missing. Please add it to your gradle.properties file.");
            return;
        }

        String prompt = "You are a study assistant. Given the notes below, " +
            "generate exactly " + count + " flashcards and " + count +
            " quiz questions. Respond ONLY with valid JSON in this exact format:\n" +
            "{\"flashcards\": [{\"question\": \"...\", \"answer\": \"...\"}],\n" +
            " \"quiz\": [{\"question\": \"...\",\n" +
            "   \"options\": [\"A\",\"B\",\"C\",\"D\"]," +
            "   \"correct_index\": 0}]}\n\nNOTES:\n" + notes;

        try {
            JSONObject body = new JSONObject();
            body.put("model", "claude-3-sonnet-20240229");
            body.put("max_tokens", 4096);
            body.put("messages", new JSONArray().put(
                new JSONObject().put("role", "user").put("content", prompt)));

            Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", API_KEY)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (!response.isSuccessful()) {
                        callback.onError("API Error (" + response.code() + "): " + responseBody);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String text = json.getJSONArray("content")
                            .getJSONObject(0).getString("text");
                        
                        // Clean the text in case Claude adds markdown backticks
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
                        callback.onError("Parse error: " + e.getMessage() + "\nResponse: " + responseBody);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("Request error: " + e.getMessage());
        }
    }
}