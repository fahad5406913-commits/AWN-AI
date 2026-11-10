package com.awnai.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

public class MainActivity extends Activity {

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setGravity(Gravity.CENTER);
        main.setPadding(dp(24), dp(24), dp(24), dp(24));
        main.setBackgroundColor(Color.rgb(12, 17, 30));

        TextView title = new TextView(this);
        title.setText("AWN AI");
        title.setTextSize(34);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("PERSONAL AI ASSISTANT");
        status.setTextSize(13);
        status.setTextColor(Color.rgb(0, 220, 190));
        status.setGravity(Gravity.CENTER);

        TextView welcome = new TextView(this);
        welcome.setText("Hello! I am AWN.\nYour personal AI assistant.");
        welcome.setTextSize(19);
        welcome.setTextColor(Color.WHITE);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, dp(40), 0, dp(40));

        EditText input = new EditText(this);
        input.setHint("Message AWN AI...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setSingleLine(true);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackgroundColor(Color.rgb(35, 43, 60));

        Button send = new Button(this);
        send.setText("SEND");
        send.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                welcome.setText("You: " + message + "\n\nAI connection will be added soon.");
                input.setText("");
            }
        });

        main.addView(title);
        main.addView(status);
        main.addView(welcome);
        main.addView(input, new LinearLayout.LayoutParams(
            -1, dp(55)
        ));
        main.addView(send);

        setContentView(main);
    }
}
