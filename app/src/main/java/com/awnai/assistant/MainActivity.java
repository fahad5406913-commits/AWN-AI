package com.awnai.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private LinearLayout chatLayout;
    private EditText messageInput;
    private EditText apiInput;
    private TextView status;
    private String apiKey = "";

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            apiKey = ApiKeyStore.load(this);
        } catch (Exception e) {
            apiKey = "";
        }

        buildInterface();
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(12));
        root.setBackgroundColor(Color.rgb(10, 15, 27));

        TextView title = new TextView(this);
        title.setText("AWN AI");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);

        status = new TextView(this);
        status.setText("PERSONAL AI ASSISTANT");
        status.setTextSize(11);
        status.setTextColor(Color.rgb(0, 220, 190));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(4), 0, dp(12));

        LinearLayout apiPanel = new LinearLayout(this);
        apiPanel.setOrientation(LinearLayout.VERTICAL);
        apiPanel.setPadding(dp(10), dp(8), dp(10), dp(8));
        apiPanel.setBackgroundColor(Color.rgb(28, 36, 52));

        TextView apiTitle = new TextView(this);
        apiTitle.setText("GEMINI API KEY");
        apiTitle.setTextColor(Color.WHITE);
        apiTitle.setTextSize(12);

        apiInput = new EditText(this);
        apiInput.setHint("Enter your Gemini API Key");
        apiInput.setSingleLine(true);
        apiInput.setTextColor(Color.WHITE);
        apiInput.setHintTextColor(Color.GRAY);
        apiInput.setInputType(129);
        apiInput.setText(apiKey);

        Button saveKey = new Button(this);
        saveKey.setText("SAVE API KEY");
        saveKey.setOnClickListener(v -> saveApiKey());

        apiPanel.addView(apiTitle);
        apiPanel.addView(apiInput);
        apiPanel.addView(saveKey);

        chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        chatLayout.setPadding(dp(8), dp(8), dp(8), dp(8));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(17, 24, 39));
        scroll.addView(chatLayout);

        TextView welcome = new TextView(this);
        welcome.setText("Hello! I am AWN.\nYour personal AI assistant.");
        welcome.setTextColor(Color.WHITE);
        welcome.setTextSize(17);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(dp(12), dp(30), dp(12), dp(30));
        chatLayout.addView(welcome);

        LinearLayout inputPanel = new LinearLayout(this);
        inputPanel.setOrientation(LinearLayout.HORIZONTAL);
        inputPanel.setGravity(Gravity.CENTER_VERTICAL);

        messageInput = new EditText(this);
        messageInput.setHint("Message AWN AI...");
        messageInput.setTextColor(Color.WHITE);
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setSingleLine(false);
        messageInput.setMaxLines(3);
        messageInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        messageInput.setBackgroundColor(Color.rgb(35, 43, 60));

        Button send = new Button(this);
        send.setText("SEND");
        send.setOnClickListener(v -> sendMessage());

        inputPanel.addView(messageInput,
            new LinearLayout.LayoutParams(0, dp(55), 1));
        inputPanel.addView(send);

        root.addView(title);
        root.addView(status);
        root.addView(apiPanel);
        root.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(inputPanel);

        setContentView(root);

        status.setText(apiKey.isEmpty()
            ? "API KEY REQUIRED"
            : "GEMINI AI READY");
    }

    private void saveApiKey() {
        String enteredKey = apiInput.getText().toString().trim();

        if (enteredKey.isEmpty()) {
            Toast.makeText(this, "Enter your API Key",
                Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ApiKeyStore.save(this, enteredKey);
            apiKey = enteredKey;
            status.setText("GEMINI AI READY");
            Toast.makeText(this, "API Key saved securely",
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Key save failed: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void addMessage(String label, String message, int color) {
        TextView bubble = new TextView(this);
        bubble.setText(label + "\n" + message);
        bubble.setTextColor(color);
        bubble.setTextSize(15);
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(5), 0, dp(5));

        chatLayout.addView(bubble, params);
    }

    private void sendMessage() {
        String prompt = messageInput.getText().toString().trim();

        if (prompt.isEmpty()) return;

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "First save your Gemini API Key",
                Toast.LENGTH_LONG).show();
            return;
        }

        messageInput.setText("");
        addMessage("YOU", prompt, Color.WHITE);

        status.setText("AWN IS THINKING...");
        addMessage("AWN", "Thinking...", Color.rgb(0, 220, 190));

        GeminiClient.sendMessage(apiKey, prompt,
            new GeminiClient.Callback() {
                @Override
                public void onSuccess(String answer) {
                    chatLayout.removeViewAt(chatLayout.getChildCount() - 1);
                    addMessage("AWN", answer,
                        Color.rgb(0, 220, 190));
                    status.setText("GEMINI AI READY");
                }

                @Override
                public void onError(String error) {
                    chatLayout.removeViewAt(chatLayout.getChildCount() - 1);
                    addMessage("ERROR", error, Color.RED);
                    status.setText("CONNECTION ERROR");
                }
            });
    }
}
