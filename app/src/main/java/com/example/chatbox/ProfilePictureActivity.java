package com.example.chatbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProfilePictureActivity extends AppCompatActivity {
Toolbar toolbar;
Button button;
ImageView imageView;
StorageReference reference, imageRef;
FirebaseStorage storage;
DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
         mReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        imageRef = reference.child("images/" + FirebaseAuth.getInstance().getUid() + ".jpg");

        button = findViewById(R.id.fragment_profile_pic_change_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == RESULT_OK){
                Uri image = data.getData();

                try {
                    imageView = findViewById(R.id.fragment_profile_picture);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    imageView.setImageBitmap(bitmap);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream);
                    byte[] byteArray = stream.toByteArray();

                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Setting Profile Picture...");
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);

                    final StorageReference ref = storage.getReference().child("images/"+FirebaseAuth.getInstance().getUid());
                    ref.putBytes(byteArray)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.e("Uri", "onSuccess: " + uri.toString());
                                               mReference.child("USER PROFILE").child(FirebaseAuth.getInstance().getUid())
                                                    .child("PROFILE LINK").setValue(uri.toString());
                                               mReference.child("PROFILE PICTURE LEDGER").child(FirebaseAuth.getInstance().getUid())
                                                       .setValue(0);
                                        }
                                    });
                                    progressDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.show();
                                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                }
                            });
                }

                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }
        }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfilePictureActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();

    }
void AsyncDatabaseImage(){
    }
}
