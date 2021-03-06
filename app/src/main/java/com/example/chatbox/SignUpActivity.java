package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
EditText userName, emailId, mPasswordOne, mPasswordTwo;
TextView userNameWarning, emailIdWarning, passwordOneWarning, passwordTwoWarning;
Button submit;
String mName = "", mEmail = "", mPassword = "", mPasswordReEnter = "";
int nameFlag = 0, emailFlag = 0, passwordFlag = 0, passwordTwoFlag = 0;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userName = findViewById(R.id.user_name_sign_up);
        emailId = findViewById(R.id.email_id);
        mPasswordOne = findViewById(R.id.password_create_one);
        mPasswordTwo = findViewById(R.id.password_create_two);

        userNameWarning = findViewById(R.id.user_name_error_sign_up);
        emailIdWarning = findViewById(R.id.email_error_sign_up);
        passwordOneWarning = findViewById(R.id.password_one_error_sign_up);
        passwordTwoWarning = findViewById(R.id.password_two_error_sign_up);

        submit = findViewById(R.id.sign_up_create_button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName = userName.getText().toString();
                if (mName.isEmpty()) {
                    userNameWarning.setText("* Name Field Empty");
                    nameFlag++;
                } else if (mName.length() < 3) {
                    userNameWarning.setText("* Name Size Insufficient");
                    nameFlag++;
                } else {
                    userNameWarning.setText("");
                    nameFlag = 0;
                }
                mEmail = emailId.getText().toString();
                Pattern pattern = Pattern.compile("\\\\b[\\\\w.%-]+@[-.\\\\w]+\\\\.[A-Za-z]{2,4}\\\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(mEmail);

                if (mEmail.isEmpty()) {
                    emailIdWarning.setText("* Email Id Empty");
                    emailFlag++;
                } else if (matcher.find()) {
                    emailIdWarning.setText("* Invalid Email");
                    emailFlag++;
                } else {
                    emailIdWarning.setText("");
                    emailFlag = 0;
                }

                mPassword = mPasswordOne.getText().toString();
                mPasswordReEnter = mPasswordTwo.getText().toString();

                if (!mPassword.equals(mPasswordReEnter)) {
                    passwordFlag++;
                    passwordTwoFlag++;
                    passwordOneWarning.setText("*Password Don't Match");

                } else if (mPassword.isEmpty()) {
                    passwordFlag++;
                    passwordTwoFlag++;
                    passwordOneWarning.setText("Password Fiels(s) Empty");
                } else if (mPasswordReEnter.isEmpty()) {
                    passwordFlag++;
                    passwordTwoFlag++;
                    passwordOneWarning.setText("Password Fiels(s) Empty");
                } else {
                    passwordFlag = 0;
                    passwordTwoFlag = 0;
                    passwordOneWarning.setText("");
                }

                if (nameFlag == 0 && emailFlag == 0 && passwordFlag == 0 && passwordTwoFlag == 0) {
                    registerEmail(mName, mEmail, mPassword);
                }
            }

                    public void registerEmail(final String name, String email, String password) {
                        mAuth = FirebaseAuth.getInstance();
                        mDatabase = FirebaseDatabase.getInstance();
                        final DatabaseReference mRef = mDatabase.getReference();
                        Toast.makeText(SignUpActivity.this, email, Toast.LENGTH_SHORT).show();
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mRef.child("USER PROFILE").child(mAuth.getUid()).child("NAME").setValue(name);

                                    Intent intent = new Intent(SignUpActivity.this, HomePageActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(SignUpActivity.this, "Shit", Toast.LENGTH_SHORT).show();
                                    if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                                        Log.e("MAINLOGGG", "onComplete: " + task.getException().getMessage());
                                    }
                                }
                            }
                        });
                    }
        });
    }


}
