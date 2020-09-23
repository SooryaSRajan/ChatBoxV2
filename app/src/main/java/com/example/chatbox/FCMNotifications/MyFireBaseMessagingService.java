package com.example.chatbox.FCMNotifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.chatbox.ChatListActivity;
import com.example.chatbox.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

@SuppressLint("Registered")
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        //sendNotification(data.get("body"), data.get("title"), data.get("userKey"));
    }

    private void sendNotification(String body, String name, String key) {

        Intent intent = new Intent(this, ChatListActivity.class);
        intent.putExtra("KEY", key);
        intent.putExtra("NAME", "userName");
        String[] names = name.split(":");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Uri defSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MyFireBaseMessagingService.this, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_message_black_24dp)
                        .setContentTitle(name + key)
                        .setVibrate(new long[] { 1000, 1000})
                        .setContentText(body)
                        .setSound(defSound)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(6 /* ID of notification*/ , notificationBuilder.build());
    }

    }

