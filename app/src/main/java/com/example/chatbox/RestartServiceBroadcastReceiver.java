package com.example.chatbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: Broadcast received");
        context.startService(new Intent(context, NotificationComponentService.class));
    }
}
