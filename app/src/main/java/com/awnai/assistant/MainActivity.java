package com.awnai.assistant;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.*;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private LinearLayout root;
    private FrameLayout contentFrame;
    private LinearLayout chatLayout;
    private EditText messageInput, apiInput;
    private EditText nameInput, ageInput, infoInput;
    private TextView status;
    private HologramFaceView faceView;

    private String apiKey = "";
    private String currentPage = "home";

    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    private static final int REQUEST_SPEECH = 1001;

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density);
    }

    private int color(String hex) {
        return Color.parseColor(hex);
    }

    private GradientDrawable bg(String c1, String c2, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{color(c1), color(c2)}
        );
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), color("#334D78"));
        return d;
    }

    private TextView text(String value, int size, String c) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color(c));
        return t;
    }

    private TextView button(String label, String c1, String c2) {
        TextView b = text(label, 15, "#FFFFFF");
        b.setGravity(Gravity.CENTER);
        b.setTypeface(null, Typeface.BOLD);
        b.setPadding(dp(16), dp(13), dp(16), dp(13));
        b.setBackground(bg(c1, c2, 18));
        b.setClickable(true);
        b.setFocusable(true);
        return b;
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int r = textToSpeech.setLanguage(new Locale("bn", "BD"));
                if (r == TextToSpeech.LANG_MISSING_DATA ||
                    r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    r = textToSpeech.setLanguage(new Locale("bn"));
                }

                ttsReady = r != TextToSpeech.LANG_MISSING_DATA &&
                           r != TextToSpeech.LANG_NOT_SUPPORTED;

                textToSpeech.setOnUtteranceProgressListener(
                    new UtteranceProgressListener() {
                        @Override
                        public void onStart(String id) {
                            runOnUiThread(() -> {
                                if (faceView != null) faceView.setMode("talking");
                                if (status != null) status.setText("AWN IS SPEAKING");
                            });
                        }

                        @Override
                        public void onDone(String id) {
                            runOnUiThread(() -> {
                                if (faceView != null) faceView.setMode("idle");
                                if (status != null) status.setText("AWN IS READY");
                            });
                        }

                        @Override
                        public void onError(String id) {
                            runOnUiThread(() -> {
                                if (faceView != null) faceView.setMode("idle");
                            });
                        }
                    }
                );
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            apiKey = ApiKeyStore.load(this);
        } catch (Exception e) {
            apiKey = "";
        }

        initializeTextToSpeech();
        buildInterface();
    }

    private void buildInterface() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(8));
        root.setBackgroundColor(color("#070B18"));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(5), dp(8), dp(5), dp(12));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);

        TextView title = text("AWN AI", 25, "#FFFFFF");
        title.setTypeface(null, Typeface.BOLD);

        status = text(apiKey.isEmpty() ? "API KEY REQUIRED" : "PERSONAL AI ONLINE",
                      10, "#54DFFF");

        brand.addView(title);
        brand.addView(status);

        TextView online = text("● ONLINE", 11, "#56E8C1");
        online.setGravity(Gravity.CENTER);

        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(online);

        contentFrame = new FrameLayout(this);

        LinearLayout navigation = new LinearLayout(this);
        navigation.setGravity(Gravity.CENTER);
        navigation.setPadding(0, dp(10), 0, dp(5));
        navigation.setBackground(bg("#101A2D", "#0B1221", 20));

        TextView home = navButton("⌂\nHOME");
        TextView chat = navButton("▤\nCHAT");
        TextView settings = navButton("⚙\nSETTINGS");

        home.setOnClickListener(v -> showPage("home"));
        chat.setOnClickListener(v -> showPage("chat"));
        settings.setOnClickListener(v -> showPage("settings"));

        navigation.addView(home, new LinearLayout.LayoutParams(0, dp(55), 1));
        navigation.addView(chat, new LinearLayout.LayoutParams(0, dp(55), 1));
        navigation.addView(settings, new LinearLayout.LayoutParams(0, dp(55), 1));

        root.addView(header);
        root.addView(contentFrame,
            new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(navigation);

        setContentView(root);
        showPage("home");
    }

    private TextView navButton(String label) {
        TextView b = text(label, 12, "#B5D7F5");
        b.setGravity(Gravity.CENTER);
        b.setTypeface(null, Typeface.BOLD);
        return b;
    }

    private void showPage(String page) {
        currentPage = page;
        contentFrame.removeAllViews();

        if (page.equals("home")) buildHomePage();
        else if (page.equals("chat")) buildChatPage();
        else buildSettingsPage();
    }

    private void buildHomePage() {
        LinearLayout home = new LinearLayout(this);
        home.setOrientation(LinearLayout.VERTICAL);
        home.setGravity(Gravity.CENTER);
        home.setPadding(dp(8), dp(12), dp(8), dp(12));

        TextView heading = text("YOUR PERSONAL AI", 12, "#61CFFF");
        heading.setGravity(Gravity.CENTER);
        heading.setLetterSpacing(0.15f);

        TextView sub = text("A new intelligence experience", 13, "#829BB9");
        sub.setGravity(Gravity.CENTER);

        faceView = new HologramFaceView(this);

        LinearLayout.LayoutParams faceParams =
            new LinearLayout.LayoutParams(dp(290), dp(290));
        faceParams.gravity = Gravity.CENTER;
        faceParams.topMargin = dp(12);
        faceParams.bottomMargin = dp(12);

        TextView listen = text("TAP TO SPEAK", 13, "#7DDFFF");
        listen.setGravity(Gravity.CENTER);
        listen.setTypeface(null, Typeface.BOLD);

        TextView mic = button("🎙", "#126A9B", "#12365D");
        mic.setTextSize(32);
        mic.setPadding(dp(25), dp(18), dp(25), dp(18));
        mic.setBackground(bg("#087DAF", "#123A76", 60));
        mic.setOnClickListener(v -> startVoiceInput());

        TextView hint = text("Press the microphone and talk to AWN",
                             12, "#829BB9");
        hint.setGravity(Gravity.CENTER);

        home.addView(heading);
        home.addView(sub);
        home.addView(faceView, faceParams);
        home.addView(listen);
        LinearLayout.LayoutParams micParams =
            new LinearLayout.LayoutParams(dp(95), dp(85));
        micParams.gravity = Gravity.CENTER;
        micParams.topMargin = dp(10);
        home.addView(mic, micParams);
        home.addView(hint);

        contentFrame.addView(home);
    }

    private void buildChatPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);

        TextView heading = text("CHAT WITH AWN", 17, "#FFFFFF");
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(dp(5), dp(10), dp(5), dp(12));
        page.addView(heading);

        chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        chatLayout.setPadding(dp(10), dp(10), dp(10), dp(10));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(bg("#10192A", "#0C1423", 18));
        scroll.addView(chatLayout);

        TextView welcome = text(
            "Hello! I am AWN.\nYour personal AI assistant.",
            16, "#8CEBFF"
        );
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(dp(12), dp(30), dp(12), dp(30));
        chatLayout.addView(welcome);

        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(0, dp(8), 0, dp(4));

        messageInput = new EditText(this);
        messageInput.setHint("Message AWN...");
        messageInput.setTextColor(Color.WHITE);
        messageInput.setHintTextColor(color("#829BB9"));
        messageInput.setTextSize(14);
        messageInput.setMaxLines(3);
        messageInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        messageInput.setBackground(bg("#17243A", "#111B2D", 15));

        TextView mic = button("🎙", "#087DAF", "#123A76");
        mic.setOnClickListener(v -> startVoiceInput());

        TextView send = button("SEND", "#087DAF", "#123A76");
        send.setOnClickListener(v -> sendMessage());

        inputRow.addView(messageInput,
            new LinearLayout.LayoutParams(0, dp(53), 1));
        inputRow.addView(mic);
        inputRow.addView(send);

        page.addView(inputRow);
        contentFrame.addView(page);
    }

    private void buildSettingsPage() {
        ScrollView scroll = new ScrollView(this);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(20));

        TextView heading = text("SETTINGS", 21, "#FFFFFF");
        heading.setTypeface(null, Typeface.BOLD);
        panel.addView(heading);

        panel.addView(section("PERSONAL PROFILE"));

        nameInput = settingInput("Your name");
        ageInput = settingInput("Your age");
        infoInput = settingInput("Your preferences and other details");

        android.content.SharedPreferences prefs =
            getSharedPreferences("awn_profile", MODE_PRIVATE);

        nameInput.setText(prefs.getString("name", ""));
        ageInput.setText(prefs.getString("age", ""));
        infoInput.setText(prefs.getString("info", ""));

        panel.addView(nameInput);
        panel.addView(ageInput);
        panel.addView(infoInput);

        TextView saveProfile = button("SAVE MY PROFILE", "#087DAF", "#123A76");
        saveProfile.setOnClickListener(v -> saveProfile());
        panel.addView(saveProfile);

        panel.addView(section("GEMINI API"));

        apiInput = settingInput("Enter Gemini API Key");
        apiInput.setSingleLine(true);
        apiInput.setInputType(129);
        apiInput.setText(apiKey);
        panel.addView(apiInput);

        TextView saveKey = button("SAVE API KEY", "#087DAF", "#123A76");
        saveKey.setOnClickListener(v -> saveApiKey());
        panel.addView(saveKey);

        panel.addView(section("ANDROID PERMISSIONS"));

        addSettingAction(panel, "MICROPHONE PERMISSION",
            "Open app settings", () ->
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()))));

        addSettingAction(panel, "FLOATING AVATAR / OVERLAY",
            "Allow display over other apps", () -> {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                }
            });

        addSettingAction(panel, "ACCESSIBILITY SERVICE",
            "Open accessibility settings", () ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        addSettingAction(panel, "NOTIFICATIONS",
            "Open notification settings", () ->
                startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())));

        TextView note = text(
            "Note: Android requires you to enable special permissions manually. "
            + "Accessibility and overlay features are not active until implemented and enabled.",
            12, "#91A9C4"
        );
        note.setPadding(dp(5), dp(15), dp(5), dp(10));
        panel.addView(note);

        scroll.addView(panel);
        contentFrame.addView(scroll);
    }

    private TextView section(String label) {
        TextView t = text(label, 13, "#61DFFF");
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(5), dp(22), dp(5), dp(10));
        return t;
    }

    private EditText settingInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(color("#829BB9"));
        e.setTextSize(14);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackground(bg("#17243A", "#111B2D", 14));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(9);
        e.setLayoutParams(p);
        return e;
    }

    private void addSettingAction(LinearLayout panel, String title,
                                  String subtitle, Runnable action) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(12), dp(12), dp(12));
        item.setBackground(bg("#17243A", "#111B2D", 14));

        TextView t = text(title, 13, "#FFFFFF");
        t.setTypeface(null, Typeface.BOLD);
        TextView s = text(subtitle, 11, "#8CA9C8");

        item.addView(t);
        item.addView(s);
        item.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(8);
        panel.addView(item, p);
    }

    private void saveProfile() {
        getSharedPreferences("awn_profile", MODE_PRIVATE)
            .edit()
            .putString("name", nameInput.getText().toString().trim())
            .putString("age", ageInput.getText().toString().trim())
            .putString("info", infoInput.getText().toString().trim())
            .apply();

        Toast.makeText(this, "Profile and AI memory saved",
            Toast.LENGTH_SHORT).show();
    }

    private void saveApiKey() {
        String key = apiInput.getText().toString().trim();

        if (key.isEmpty()) {
            Toast.makeText(this, "Enter your API Key",
                Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ApiKeyStore.save(this, key);
            apiKey = key;
            status.setText("PERSONAL AI ONLINE");
            Toast.makeText(this, "API Key saved securely",
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Key save failed: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition unavailable",
                Toast.LENGTH_LONG).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_SPEECH
            );
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "কথা বলুন...");

        if (faceView != null) faceView.setMode("listening");
        if (status != null) status.setText("AWN IS LISTENING");

        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (Exception e) {
            if (faceView != null) faceView.setMode("idle");
            Toast.makeText(this, "Voice input could not start",
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SPEECH) {
            if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                if (faceView != null) faceView.setMode("idle");
                Toast.makeText(this, "Microphone permission required",
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH) {
            if (faceView != null) faceView.setMode("idle");

            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (results != null && !results.isEmpty()) {
                    boolean showInChat = currentPage.equals("chat");

                    if (showInChat && messageInput != null) {
                        messageInput.setText(results.get(0));
                    }

                    sendPrompt(results.get(0), showInChat);
                } else if (status != null) {
                    status.setText("AWN IS READY");
                }
            } else if (status != null) {
                status.setText("AWN IS READY");
            }
        }
    }

    private void sendMessage() {
        if (messageInput == null) return;
        String prompt = messageInput.getText().toString().trim();
        if (prompt.isEmpty()) return;

        messageInput.setText("");
        sendPrompt(prompt, true);
    }

    private void sendPrompt(String prompt, boolean showInChat) {
        if (prompt == null || prompt.trim().isEmpty()) return;

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "First save your Gemini API Key in Settings",
                Toast.LENGTH_LONG).show();
            showPage("settings");
            return;
        }

        LinearLayout targetChat = chatLayout;

        if (showInChat && targetChat != null) {
            addMessage(targetChat, "YOU", prompt, "#FFFFFF");
            addMessage(targetChat, "AWN", "Thinking...", "#54DFFF");
        }

        if (status != null) status.setText("AWN IS THINKING");
        if (faceView != null) faceView.setMode("thinking");

        android.content.SharedPreferences prefs =
            getSharedPreferences("awn_profile", MODE_PRIVATE);

        String name = prefs.getString("name", "");
        String age = prefs.getString("age", "");
        String info = prefs.getString("info", "");

        String memory = "\n\nPersonal context about the user (use naturally when relevant):"
            + "\nName: " + name
            + "\nAge: " + age
            + "\nPreferences and details: " + info;

        String fullPrompt = prompt + memory;

        GeminiClient.sendMessage(apiKey, fullPrompt,
            new GeminiClient.Callback() {
                @Override
                public void onSuccess(String answer) {
                    runOnUiThread(() -> {
                        if (showInChat && targetChat != null) {
                            int last = targetChat.getChildCount() - 1;
                            if (last >= 0) targetChat.removeViewAt(last);
                            addMessage(targetChat, "AWN", answer, "#70E7FF");
                        }

                        if (faceView != null) faceView.setMode("talking");
                        speakText(answer);

                        if (status != null) status.setText("AWN IS READY");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        if (showInChat && targetChat != null) {
                            int last = targetChat.getChildCount() - 1;
                            if (last >= 0) targetChat.removeViewAt(last);
                            addMessage(targetChat, "ERROR", error, "#FF7777");
                        }

                        if (faceView != null) faceView.setMode("idle");
                        if (status != null) status.setText("CONNECTION ERROR");
                    });
                }
            });
    }

    private void addMessage(LinearLayout target, String label,
                            String message, String textColor) {
        TextView bubble = text(label + "\n" + message, 14, textColor);
        bubble.setPadding(dp(13), dp(11), dp(13), dp(11));
        bubble.setBackground(bg("#17243A", "#111B2D", 14));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(5), 0, dp(5));
        target.addView(bubble, p);
    }

    private void speakText(String value) {
        if (textToSpeech != null && ttsReady) {
            textToSpeech.speak(value, TextToSpeech.QUEUE_FLUSH,
                null, "AWN_RESPONSE");
        } else if (faceView != null) {
            faceView.setMode("idle");
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
