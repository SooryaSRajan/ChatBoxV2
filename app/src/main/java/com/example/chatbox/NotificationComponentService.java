package com.example.chatbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class NotificationComponentService extends Service {
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("UNREAD MESSAGE");
    DatabaseReference mRefOnline = FirebaseDatabase.getInstance().getReference().child("ONLINE");
    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    Boolean serviceStatus = false;
    Context context;
    int num = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NotificationComponentService(Context context){
        super();
       // this.context = context;
        Log.e(TAG, "NotificationComponent: Service Started" );
    }

    public NotificationComponentService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "NotificationComponent: Service Started onStart" );
        final long[] vibrate = {10, 20 ,100, 50};
        if(!serviceStatus) {
            serviceStatus = true;
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String key = snap.child("TO").getValue().toString();
                        if (mAuth != null) {
                            Log.e(TAG, "onDataChange: Data Change");
                            if (key.contains(mAuth.getUid())) {
                                Log.e(TAG, "onDataChange: Data Change");
                                Intent intent = new Intent(NotificationComponentService.this, ChatListActivity.class);
                                intent.putExtra("NAME", snap.child("NAME").getValue().toString());
                                intent.putExtra("KEY", snap.child("FROM").getValue().toString());
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                final Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                NotificationCompat.Builder notificationBuilder =
                                        new NotificationCompat.Builder(NotificationComponentService.this, "CHANNEL_ID")
                                                .setSmallIcon(R.drawable.logout_icon_24dp)
                                                .setContentTitle("ChatBox")
                                                .setContentIntent(pendingIntent)
                                                .setVibrate(vibrate)
                                                .setSound(notificationSound)
                                                .setContentText(snap.child("NAME").getValue().toString() + " : " + snap.child("MESSAGE").getValue().toString())
                                                .setAutoCancel(true);

                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel("CHANNEL_ID",
                                            "Channel human readable title",
                                            NotificationManager.IMPORTANCE_DEFAULT);

                                    notificationManager.createNotificationChannel(channel);
                                }
                                Random rand = new Random();

                                // Generate random integers in range 0 to 999
                                int randInt = rand.nextInt(1000);

                                notificationManager.notify(randInt /* ID of notification*/, notificationBuilder.build());

                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mRef.addValueEventListener(listener);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy: Destroy Service called");

        serviceStatus = false;
        Intent intent = new Intent("ReceiveNotificationBroadcast");
        sendBroadcast(intent);
    }
}
