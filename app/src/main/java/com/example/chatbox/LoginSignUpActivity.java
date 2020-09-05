package com.example.chatbox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginSignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Intent bIntent = new Intent("ReceiveNotificationBroadcast");
            sendBroadcast(bIntent);

            Intent broadcastIntent = new Intent(LoginSignUpActivity.this, RestartServiceBroadcastReceiver.class);
            PendingIntent  pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 123, broadcastIntent, 0);
            long startTime = System.currentTimeMillis();
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime, 1000 * 60, pendingIntent);

            Intent intent = new Intent(LoginSignUpActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        Button button = findViewById(R.id.sign_up_button);
        Button button2 = findViewById(R.id.login_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignUpActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
