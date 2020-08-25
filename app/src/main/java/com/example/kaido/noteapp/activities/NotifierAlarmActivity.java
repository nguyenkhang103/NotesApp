package com.example.kaido.noteapp.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.dao.NoteDAO;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;

import java.util.Date;


public class NotifierAlarmActivity extends BroadcastReceiver {
    NoteDB noteDB;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        noteDB = NoteDB.getNoteDB(context.getApplicationContext());
        NoteDAO noteDAO = noteDB.noteDAO();
        Note note = new Note();
        note.setTitle(intent.getStringExtra("note title"));
        note.setTimeReminder(new Date(intent.getStringExtra("time reminder")));
        note.setId(intent.getIntExtra("id",0));
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context,MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent1);
        PendingIntent intent2 = taskStackBuilder.getPendingIntent(1,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            channel = new NotificationChannel("my_channel_01","hello", NotificationManager.IMPORTANCE_HIGH);
        }

        Notification notification = builder.setContentTitle("Time Reminder")
                .setContentText(intent.getStringExtra("note title")).setAutoCancel(true)
                .setSound(alarmSound).setSmallIcon(R.mipmap.icon)
                .setContentIntent(intent2)
                .setChannelId("my_channel_01")
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);

    }



}
