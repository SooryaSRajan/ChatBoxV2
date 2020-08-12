package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginActivity extends AppCompatActivity {
String mEmail, mPassword;
EditText editTextMail, editTextPassword;
Button signIn;
int flag1, flag2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        signIn = findViewById(R.id.sign_in_button);
        editTextMail = findViewById(R.id.user_email_sign_in);
        editTextPassword = findViewById(R.id.user_password_sign_in);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag1 = 0;
                flag2 = 0;
                mEmail = editTextMail.getText().toString();
                mPassword = editTextPassword.getText().toString();

                if(mEmail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Email Field Empty", Toast.LENGTH_SHORT).show();
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

    }
    public void loginUser(String email, String password){

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER PROFILE");
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
}
