package com.example.chatbox.FCMNotifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.example.chatbox.ChatListActivity;
import com.example.chatbox.HomePageActivity;
import com.example.chatbox.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

@SuppressLint("Registered")
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        sendNotification(data.get("body"), data.get("title"));
    }

    private void sendNotification(String body, String name) {

        if (FirebaseAuth.getInstance().getUid() != null) {
            Intent intent = new Intent(this, HomePageActivity.class);

            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            assert v != null;
            v.vibrate(500);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Uri defSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(MyFireBaseMessagingService.this, "CHANNEL_ID")
                            .setSmallIcon(R.drawable.ic_message_black_24dp)
                            .setContentTitle(name)
                            .setVibrate(new long[]{1000, 1000})
                            .setContentText(body)
                            .setSound(defSound)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setDefaults(Notification.DEFAULT_VIBRATE);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel("CHANNEL_ID",
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);

            Random random = new Random();
            notificationManager.notify(random.nextInt(6000-100), notificationBuilder.build());
        }
    }
    }

