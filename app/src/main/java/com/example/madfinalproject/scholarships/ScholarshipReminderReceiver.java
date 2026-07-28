package com.example.madfinalproject.scholarships;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.madfinalproject.R;

public final class ScholarshipReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL="scholarship_reminders";
    @Override public void onReceive(Context context,Intent intent){String title=intent.getStringExtra("title"),university=intent.getStringExtra("university"),link=intent.getStringExtra("link"),kind=intent.getStringExtra("kind");NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Scholarship Reminders",NotificationManager.IMPORTANCE_HIGH));Intent open=new Intent(Intent.ACTION_VIEW,Uri.parse(link==null||link.isEmpty()?"https://www.google.com/search?q="+Uri.encode(title):link));PendingIntent content=PendingIntent.getActivity(context,intent.getIntExtra("requestCode",0),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);String message="deadline".equals(kind)?"Deadline is approaching for "+title:title+" is now open for applications.";if(university!=null&&!university.isEmpty())message+=" — "+university;manager.notify(intent.getIntExtra("requestCode",0),new NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.outline_add_alert_24).setContentTitle("AbroadIQ Scholarship Alert").setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentIntent(content).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build());}
}
