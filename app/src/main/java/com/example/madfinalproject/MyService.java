package com.example.madfinalproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    static Ringtone ringtone;
    String channel_id = "Deadline_Notify";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent i = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id, "University Deadlines", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channel_id)
                .setContentTitle("AbroadIQ Deadline Alert")
                .setContentText("A university admission deadline is today!")
                .setSmallIcon(R.drawable.outline_add_alert_24)
                .setOngoing(true)
                .addAction(R.drawable.outline_add_alert_24, "Stop Alarm", pendingIntent)
                .build();

        startForeground(1, notification);

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (path == null) path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), path);
        ringtone.play();

        return START_NOT_STICKY;
    }

    public static void stopAlarm() {
        if (ringtone != null) ringtone.stop();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}