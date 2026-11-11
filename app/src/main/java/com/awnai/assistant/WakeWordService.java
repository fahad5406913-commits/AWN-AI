package com.awnai.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.openwakeword.OpenWakeWord;

public class WakeWordService extends Service {

    private static final String CHANNEL_ID = "awn_wake_service";
    private static final String ALERT_CHANNEL_ID = "awn_wake_detected";
    private static final int NOTIFICATION_ID = 701;

    private OpenWakeWord detector;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildServiceNotification());

        if (detector == null) {
            try {
                detector = new OpenWakeWord.Builder(this)
                    .setModel(OpenWakeWord.BuiltInModel.HEY_JARVIS)
                    .setThreshold(0.2f)
                    .setDebounceMs(2500L)
                    .build();

                detector.start(score -> showDetectedNotification());
            } catch (Exception e) {
                stopSelf();
            }
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager =
                getSystemService(NotificationManager.class);

            manager.createNotificationChannel(
                new NotificationChannel(
                    CHANNEL_ID,
                    "AWN Wake Word",
                    NotificationManager.IMPORTANCE_LOW
                )
            );

            manager.createNotificationChannel(
                new NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Hey Jarvis Detection",
                    NotificationManager.IMPORTANCE_HIGH
                )
            );
        }
    }

    private Notification buildServiceNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AWN AI")
            .setContentText("Listening for Hey Jarvis")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build();
    }

    private void showDetectedNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("WAKE_WORD_DETECTED", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            702,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT |
                (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Notification notification =
            new Notification.Builder(this, ALERT_CHANNEL_ID)
                .setContentTitle("Hey Jarvis detected!")
                .setContentText("Tap to talk to AWN AI")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager manager =
            getSystemService(NotificationManager.class);

        manager.notify(702, notification);
    }

    @Override
    public void onDestroy() {
        if (detector != null) {
            detector.stop();
            detector.release();
            detector = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
