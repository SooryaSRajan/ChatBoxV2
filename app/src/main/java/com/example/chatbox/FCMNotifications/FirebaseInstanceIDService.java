package com.example.chatbox.FCMNotifications;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

@SuppressLint("Registered")
public class FirebaseInstanceIDService extends FirebaseMessagingService {
String token;
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        token = s;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            updateToken(s);
        }
    }

    public void updateToken(String mToken){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("TOKENS");
        assert firebaseUser != null;
        reference.child(firebaseUser.getUid()).setValue(mToken);
    }


}
