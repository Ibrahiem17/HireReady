package com.example.hireready;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIEvaluator {

    private static final String OPENAI_API_KEY = ApiKeys.OPENAI_API_KEY;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public interface EvaluationCallback {
        void onSuccess(JSONObject result);
        void onFailure(String error);
    }

    public static void evaluate(
            String question,
            String answer,
            EvaluationCallback callback
    ) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject bodyJson = new JSONObject();
                bodyJson.put("model", "gpt-4o-mini");

                JSONArray messages = new JSONArray();

                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content",
                                "You are an interview evaluator. " +
                                        "Return ONLY valid JSON. Do not add explanations."
                        ));

                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content",
                                "Question:\n" + question + "\n\n" +
                                        "Answer:\n" + answer + "\n\n" +
                                        "Evaluate the answer and return ONLY JSON in this format:\n" +
                                        "{\n" +
                                        "  \"score\": number between 0 and 100,\n" +
                                        "  \"strengths\": [string],\n" +
                                        "  \"weaknesses\": [string],\n" +
                                        "  \"improvements\": [string]\n" +
                                        "}"
                        ));

                bodyJson.put("messages", messages);

                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(OPENAI_URL)
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    callback.onFailure("OpenAI API failed");
                    return;
                }

                String responseString = response.body().string();

                JSONObject full = new JSONObject(responseString);
                String content = full
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                callback.onSuccess(new JSONObject(content));

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
    }
}
