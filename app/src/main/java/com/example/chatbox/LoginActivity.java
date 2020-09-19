package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatbox.MessageDatabase.MessageData;
import com.example.chatbox.MessageDatabase.MessageDatabase;
import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginActivity extends AppCompatActivity {
String mEmail, mPassword;
EditText editTextMail, editTextPassword;
Button signIn, forgotPassword;
int flag1, flag2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        Log.e(TAG, "onCreate: Login Activity");

        signIn = findViewById(R.id.sign_in_button);
        editTextMail = findViewById(R.id.user_email_sign_in);
        editTextPassword = findViewById(R.id.user_password_sign_in);
        forgotPassword = findViewById(R.id.forgot_password);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag1 = 0;
                flag2 = 0;
                mEmail = editTextMail.getText().toString();
                mPassword = editTextPassword.getText().toString();
                Pattern pattern = Patterns.EMAIL_ADDRESS;

                if(mEmail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Email Field Empty", Toast.LENGTH_SHORT).show();
                    flag1++;
                }
                else if(!pattern.matcher(mEmail).matches()){
                    Toast.makeText(LoginActivity.this, "Invalid Email ID", Toast.LENGTH_SHORT).show();
                    flag1++;
                }

                if(mPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Password Field Empty", Toast.LENGTH_SHORT).show();
                    flag2++;
                }
                if(flag1 == 0 && flag2 == 0){
                    loginUser(mEmail, mPassword);
                }

            }
        });
    forgotPassword.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    });
    }

    public void loginUser(String email, String password){
        Log.e(TAG, "loginUser: InLoginUserMethod");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.e(TAG, "onComplete: Logged in" );
                    Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    final Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER PROFILE");
                    final DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("NEW MESSAGE");

                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                                asyncTask(dataSnapshot.getKey(),  dataSnapshot.child("NAME").getValue().toString());

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    messageReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) ||
                                        snap.child("FROM").getValue().toString().contains(mAuth.getUid())) {
                                        AsyncMessage(snap.getKey(), snap.child("FROM").getValue().toString(),
                                                snap.child("TO").getValue().toString(), snap.child("TIME").getValue().toString(),
                                                snap.child("MESSAGE").getValue().toString());
                                    Log.e(TAG, "onMessageAdded! " );
                                }
                            }
                        }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: " + error );
                            }
                        });
                startActivity(intent);
                finish();
            }
            }
        });
    }
    void asyncTask(final String userId, final String mName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(LoginActivity.this);
                    profile object = new profile(Objects.requireNonNull(userId), mName);
                    database.dao().insertProfile(object);


                } catch (Exception e) {
                    Log.e("Async List View", e.toString());
                }

            }
        });
    }

    void AsyncMessage(final String mKey, final String mFrom, final String mTo, final String mTime, final String mMessage){
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                MessageDatabase database = MessageDatabase.getInstance(LoginActivity.this);
                MessageData dataObject = new MessageData(mKey, mFrom, mTo, mTime, mMessage);
                database.dao().InsertMessage(dataObject);
                Log.e(TAG, "run: Message Added" );
            }
        };
        thread.start();
    }
}
