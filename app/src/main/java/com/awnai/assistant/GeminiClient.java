package com.awnai.assistant;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class GeminiClient {

    public interface Callback {
        void onSuccess(String answer);
        void onError(String error);
    }

    private static final String MODEL = "gemini-3.5-flash-lite";

    public static void sendMessage(
            String apiKey,
            String prompt,
            Callback callback
    ) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(45000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                    "Content-Type", "application/json"
                );
                connection.setRequestProperty(
                    "x-goog-api-key", apiKey
                );

                JSONObject part = new JSONObject();
                part.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(part);

                JSONObject content = new JSONObject();
                content.put("role", "user");
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject body = new JSONObject();
                body.put("contents", contents);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes("UTF-8"));
                }

                int status = connection.getResponseCode();

                InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject result = new JSONObject(response.toString());

                if (status < 200 || status >= 300) {
                    JSONObject error = result.optJSONObject("error");
                    String message = error != null
                        ? error.optString("message", "HTTP " + status)
                        : "HTTP " + status;

                    throw new Exception(message);
                }

                JSONArray candidates = result.getJSONArray("candidates");
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject responseContent =
                    candidate.getJSONObject("content");
                JSONArray answerParts =
                    responseContent.getJSONArray("parts");

                StringBuilder answer = new StringBuilder();

                for (int i = 0; i < answerParts.length(); i++) {
                    answer.append(
                        answerParts.getJSONObject(i).optString("text", "")
                    );
                }

                if (answer.length() == 0) {
                    throw new Exception("Gemini returned an empty answer.");
                }

                String finalAnswer = answer.toString();

                new Handler(Looper.getMainLooper()).post(
                    () -> callback.onSuccess(finalAnswer)
                );

            } catch (Exception e) {
                String message = e.getMessage() == null
                    ? "Connection error"
                    : e.getMessage();

                new Handler(Looper.getMainLooper()).post(
                    () -> callback.onError(message)
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
