package com.example.chatbox;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfilePictureActivity extends AppCompatActivity {
Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfilePictureActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();

    }
}
