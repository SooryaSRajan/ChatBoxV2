package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.chatbox.FCMNotifications.APIInterface;
import com.example.chatbox.FCMNotifications.NotificationBody;
import com.example.chatbox.FCMNotifications.NotificationContent;
import com.example.chatbox.FCMNotifications.RetrofitClient;
import com.example.chatbox.MessageDatabase.MessageData;
import com.example.chatbox.MessageDatabase.MessageDatabase;
import com.example.chatbox.list_adapters.ChatAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.chatbox.Constants.USER_NAME;

public class ChatListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private List<MessageData> messageData = null;
    private ImageButton backButton;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference countRef = database.getReference().child("UNREAD COUNT"), ref = database.getReference(),
            ref2 = database.getReference(), onlineRef = database.getReference(), refMain = database.getReference().child("UNREAD MESSAGE"),
            refToken;
    private ValueEventListener UnsentReceiverListener, UnsentSelfListener, TokenListener, ConcurrentTokensListener,
            CountListener, TypingListener, OnlineListener, MessageReceiverListener, ConcurrentMessageReceiverListener, ConcurrentUserNodeListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static ListView listView;
    private ArrayList<HashMap> mList = new ArrayList<>();
    private Set<String> mKeyList = new HashSet<>();
    private ArrayList<String> tokenList = new ArrayList<>();
    private static ChatAdapter adapter;
    private EditText chatText;
    private ImageButton mSend;
    private String mMessage;
    private String TAG = "ChatListActivity";
    private ArrayList<String> removeKey = new ArrayList<>();
    private String typingStatus;
    private TextView mTypingStatus, mOnlineStatus, mSeenStatus;
    private ImageButton addImage, recorderButton, recorderBack, startRecording,
            stopRecording, playRecording, pauseRecording;
    private String userKey, userName, token, concurrentFilterKey;
    private int mCount = 0;
    private NotificationContent content;
    private NotificationBody notificationBody;
    private APIInterface apiInterface;
    private int onLongClickPosition = 0;
    private String appToken;
    private ArrayList<String> concurrentMessageKeys = new ArrayList<>();
    private FirebaseStorage storage;
    private StorageReference mDataRef;
    private RelativeLayout recorderDashboard;
    boolean recorderButtonFlag = false;
    private Button sendRecording;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String finalRecorderPath = null, recordedFileName = null;
    private int playPauseStateFlag = 0, recorderStateFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recorderDashboard = findViewById(R.id.recorder_dashboard);
        recorderDashboard.setVisibility(View.GONE);

        recorderButton = findViewById(R.id.recorder_button);
        recorderBack = findViewById(R.id.exit_recorder);
        startRecording = findViewById(R.id.start_recording);
        stopRecording = findViewById(R.id.stop_recording);
        sendRecording = findViewById(R.id.send_recording);
        playRecording = findViewById(R.id.play_recording);
        pauseRecording = findViewById(R.id.pause_recording);

        stopRecording.setVisibility(View.GONE);
        sendRecording.setVisibility(View.GONE);
        playRecording.setVisibility(View.GONE);
        pauseRecording.setVisibility(View.GONE);

        recorderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkPermissionForRecorder()){
                    if(!recorderButtonFlag) {
                        addImage.setVisibility(View.GONE);
                        chatText.setVisibility(View.GONE);
                        recorderDashboard.setVisibility(View.VISIBLE);
                        startRecording.setVisibility(View.VISIBLE);
                        recorderButtonFlag = true;
                    }
                    else{
                        addImage.setVisibility(View.VISIBLE);
                        chatText.setVisibility(View.VISIBLE);
                        recorderDashboard.setVisibility(View.GONE);
                        sendRecording.setVisibility(View.GONE);
                        stopRecording.setVisibility(View.GONE);
                        startRecording.setVisibility(View.GONE);
                        playRecording.setVisibility(View.GONE);
                        pauseRecording.setVisibility(View.GONE);
                        recorderButtonFlag = false;

                        if(finalRecorderPath !=null){
                            if(playPauseStateFlag == 1){
                                playPauseStateFlag = 0;
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                Log.e(TAG, "onClick: Media player stopped" );
                            }
                            File file = new File(finalRecorderPath);
                   //         file.delete();
                            finalRecorderPath = null;
                            recordedFileName = null;
                            Log.e(TAG, "onClick: File deleted, exit from recorder dashboard" );
                        }

                        if(recorderStateFlag == 1){
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            Log.e(TAG, "onClick: Recording stopped" );
                        }

                    }
                }

                else{
                    requestPermissionForRecorder();
                }

            }
        });

        recorderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage.setVisibility(View.VISIBLE);
                chatText.setVisibility(View.VISIBLE);
                recorderDashboard.setVisibility(View.GONE);
                sendRecording.setVisibility(View.GONE);
                stopRecording.setVisibility(View.GONE);
                startRecording.setVisibility(View.GONE);
                playRecording.setVisibility(View.GONE);
                pauseRecording.setVisibility(View.GONE);

                if(finalRecorderPath !=null){
                    if(playPauseStateFlag == 1){
                        playPauseStateFlag = 0;
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        Log.e(TAG, "onClick: Media player stopped" );
                    }
                    File file = new File(finalRecorderPath);
      //              file.delete();
                    finalRecorderPath = null;
                    recordedFileName = null;
                    Log.e(TAG, "onClick: File deleted, re-recording" );
                }

                if(recorderStateFlag == 1){
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    Log.e(TAG, "onClick: Recording stopped");
                }

            }
        });

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorderStateFlag = 1;
                if(finalRecorderPath !=null){
                    if(playPauseStateFlag == 1){
                        playPauseStateFlag = 0;
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        Log.e(TAG, "onClick: Player stopped");
                    }
                    File file = new File(finalRecorderPath);
     //               file.delete();
                    finalRecorderPath = null;
                    recordedFileName = null;
                    Log.e(TAG, "onClick: File deleted, re-recording" );
                }

                startRecording.setVisibility(View.GONE);
                stopRecording.setVisibility(View.VISIBLE);
                sendRecording.setVisibility(View.GONE);
                playRecording.setVisibility(View.GONE);
                pauseRecording.setVisibility(View.GONE);

                recordedFileName = UUID.randomUUID().toString() + "_audio_record.3gpp";
                finalRecorderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() +
                        "/" + recordedFileName;
                finalRecorderPath = finalRecorderPath.trim();

                Log.e(TAG, "onClick: " + finalRecorderPath );

                setupMediaRecorder();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    Log.e(TAG, "onClick: Started recording" );
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: recorder error " + e);
                }

            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorderStateFlag = 0;
                sendRecording.setVisibility(View.VISIBLE);
                stopRecording.setVisibility(View.GONE);
                startRecording.setVisibility(View.VISIBLE);
                playRecording.setVisibility(View.VISIBLE);
                pauseRecording.setVisibility(View.GONE);
                Log.e(TAG, "onClick: Recording stopped" );
                mediaRecorder.stop();
                mediaRecorder.release();
            }
        });

        sendRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage.setVisibility(View.VISIBLE);
                chatText.setVisibility(View.VISIBLE);
                recorderDashboard.setVisibility(View.GONE);
                sendRecording.setVisibility(View.GONE);
                stopRecording.setVisibility(View.GONE);
                startRecording.setVisibility(View.GONE);
                playRecording.setVisibility(View.GONE);
                pauseRecording.setVisibility(View.GONE);
                if(playPauseStateFlag == 1){
                    Log.e(TAG, "onClick: Media player stopped" );
                    playPauseStateFlag = 0;
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                uploadMediaRecording(recordedFileName, finalRecorderPath);
                finalRecorderPath = null;
                recordedFileName = null;

            }
        });

        playRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording.setVisibility(View.GONE);
                pauseRecording.setVisibility(View.VISIBLE);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(finalRecorderPath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Log.e(TAG, "onClick: Playing");
                playPauseStateFlag = 1;
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.release();
                        playRecording.setVisibility(View.VISIBLE);
                        pauseRecording.setVisibility(View.GONE);
                        playPauseStateFlag = 0;
                        Log.e(TAG, "onCompletion: Completed playing audio");
                    }
                });
            }
        });

        pauseRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording.setVisibility(View.VISIBLE);
                pauseRecording.setVisibility(View.GONE);
                playPauseStateFlag = 0;
                Log.e(TAG, "onClick: Paused playing audio");
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });

        appToken = FirebaseInstanceId.getInstance().getToken();
        ref.child("CONCURRENT USERS").child(mAuth.getUid()).child(appToken).setValue("TOKEN");

         storage = FirebaseStorage.getInstance();
         mDataRef = storage.getReferenceFromUrl("gs://chat-box-v2.appspot.com");

        final Intent intent = getIntent();
        ref.child("ONLINE").child(mAuth.getUid()).setValue("ONLINE");
        ref.child("ONLINE").child(mAuth.getUid()).onDisconnect().setValue("OFFLINE");

        userKey = intent.getStringExtra("KEY");
        userName = intent.getStringExtra("NAME");
        FirebaseReferenceInitializer();

        listView = findViewById(R.id.chat_list_view);
        adapter = new ChatAdapter(ChatListActivity.this, mList);
        AsyncMessage();

        Log.e(TAG, "onCreate: " + USER_NAME + userName + userKey);

        ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).addValueEventListener(UnsentReceiverListener);
        ref.child("UNSENT MESSAGE KEY").child(userKey).child(mAuth.getUid()).addValueEventListener(UnsentSelfListener);
        ref.child("CONCURRENT USERS").child(mAuth.getUid()).addValueEventListener(ConcurrentUserNodeListener);
        ref.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(appToken).child(userKey).addValueEventListener(ConcurrentTokensListener);


        try {
            refToken = database.getReference().child("TOKENS").child(userKey);
            database.getReference().child("TOKENS").child(mAuth.getUid()).setValue(FirebaseInstanceId.getInstance().getToken());
            refToken.addValueEventListener(TokenListener);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Token " + e.toString());
            Toast.makeText(this, userKey, Toast.LENGTH_SHORT).show();
        }

        ref.child("TYPING").child(Objects.requireNonNull(intent.getStringExtra("KEY")))
                .child(Objects.requireNonNull(mAuth.getUid())).setValue("NOT");

        countRef.addValueEventListener(CountListener);

        toolbar = findViewById(R.id.tool_bar_chat);
        toolbar.setTitle("");
        TextView toolBarName = findViewById(R.id.toolbar_name);
        toolBarName.setText(intent.getStringExtra("NAME"));
        setSupportActionBar(toolbar);
        addImage = findViewById(R.id.add_resource);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(ChatListActivity.this);
        final View builderView = getLayoutInflater().inflate(R.layout.unsend_message_layout, null);
        builder.setView(builderView);
        final android.app.AlertDialog alertDialog = builder.create();

        builderView.findViewById(R.id.unsend_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.e(TAG, "onItemClick: List clicked");
                if (mList.get(position).get("TYPE").toString().contains("IMAGE")) {
                    Log.e(TAG, "onItemClick: Image list");
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            Log.e(TAG, "onItemClick: thread started");
                            FileInputStream fileInputStream;
                            Bitmap bitmap;

                            try {
                                fileInputStream = ChatListActivity.this.openFileInput(mList.get(position).get("MESSAGE").toString());
                                bitmap = BitmapFactory.decodeStream(fileInputStream);
                                final Bitmap finalBitmap = bitmap;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Log.e(TAG, "onItemClick: Inside post");
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ChatListActivity.this);
                                            final View builderView = getLayoutInflater().inflate(R.layout.message_image_layout_enlarged, null);
                                            ImageView imageView = builderView.findViewById(R.id.message_image_view);
                                            imageView.setImageBitmap(finalBitmap);
                                            builder.setView(builderView);
                                            final android.app.AlertDialog alertDialog = builder.create();
                                            alertDialog.show();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                Log.e(TAG, "GetImageBitmap: Bitmap");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Log.e(TAG, "run: File not found" );
                            }
                        }
                    });
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popupMenu = new PopupMenu(ChatListActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.chat_context_menu, popupMenu.getMenu());
                onLongClickPosition = position;

                try {
                    if (mList.get(position).get("FROM").toString().contains(mAuth.getUid())) {
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (onLongClickPosition == mList.size()) {
                                    --onLongClickPosition;
                                }

                                if (mList.size() == 0)
                                    Log.e(TAG, "onMenuItemClick: Mlist empty");

                                final String keys = mList.get(onLongClickPosition).get("KEY").toString();
                                if (item.getItemId() == R.id.unsend_message_menu) {
                                    Log.e(TAG, "onClick: " + keys);
                                    final String finalKeys = keys;
                                    ref.child("NEW MESSAGE").child(keys).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference refer) {

                                            ref.child("CONCURRENT MESSAGE").child(mAuth.getUid()).child(userKey).child(keys).removeValue();
                                            refMain.child(finalKeys).removeValue();
                                            mList.remove(onLongClickPosition);
                                            mKeyList.remove(keys);
                                            adapter.notifyDataSetChanged();

                                            AsyncTask.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                                                        database.dao().deleteTuple(finalKeys);
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            });

                                            ref.child("UNSENT MESSAGE KEY").child(userKey).child(mAuth.getUid()).child(finalKeys).setValue("ADDED");

                                            Log.e(TAG, "onComplete: " + mList.size() + " " + onLongClickPosition);

                                            try {
                                                if (mList.size() == onLongClickPosition) {
                                                    if (mList.get(onLongClickPosition - 1).get("TYPE").toString().contains("MESSAGE")) {
                                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(mList.get(onLongClickPosition - 1).get("MESSAGE").toString());
                                                        ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue(mList.get(onLongClickPosition - 1).get("MESSAGE").toString());
                                                    }
                                                    else if(mList.get(onLongClickPosition - 1).get("TYPE").toString().contains("IMAGE")){
                                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("you sent an image");
                                                        ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue("sent you an image");
                                                    }
                                                }
                                            } catch (Exception e) {
                                                if (mList.size() == 0) {
                                                    ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("");
                                                    ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue("");
                                                }
                                            }

                                        }
                                    });
                                }
                                if (item.getItemId() == R.id.remove_local_message) {
                                    mList.remove(onLongClickPosition);
                                    mKeyList.remove(keys);
                                    try {
                                        if (mList.size() == onLongClickPosition) {
                                            if (mList.get(onLongClickPosition - 1).get("TYPE").toString().contains("MESSAGE")) {
                                                ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(mList.get(onLongClickPosition - 1).get("MESSAGE").toString());
                                            }
                                            else if(mList.get(onLongClickPosition - 1).get("TYPE").toString().contains("IMAGE")){
                                                ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("you sent an image");
                                            }
                                        }
                                    } catch (Exception e) {
                                        if (mList.size() == 0) {
                                            ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("");
                                        }
                                    }
                                    adapter.notifyDataSetChanged();

                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                                                database.dao().deleteLocal(keys);
                                            } catch (Exception e) {

                                            }
                                        }
                                    });

                                }

                                if (item.getItemId() == R.id.copy_local_message) {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text 1", mList.get(onLongClickPosition).get("MESSAGE").toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    } else {
                        PopupMenu menu = new PopupMenu(ChatListActivity.this, view);
                        menu.getMenuInflater().inflate(R.menu.chat_copy_menu, menu.getMenu());

                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.copy_local_other_message) {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text 2", mList.get(onLongClickPosition).get("MESSAGE").toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                                return true;
                            }
                        });
                        menu.show();
                    }

                } catch (Exception e) {

                }
                return true;
            }
        });

        mOnlineStatus = findViewById(R.id.online_status);
        mTypingStatus = findViewById(R.id.typing_status);
        mSeenStatus = findViewById(R.id.seen_status);
        mTypingStatus.setVisibility(View.GONE);
        mSeenStatus.setVisibility(View.GONE);

        ref2.addValueEventListener(TypingListener);


        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String value = snapshot.child(userKey).child(mAuth.getUid()).getValue().toString();
                    String key = mList.get(mList.size() - 1).get("FROM").toString();
                    if (value.contains("0")) {
                        if (key.contains(mAuth.getUid())) {
                            mTypingStatus.setVisibility(View.GONE);
                            mSeenStatus.setVisibility(View.VISIBLE);
                            mSeenStatus.setText("seen");
                        } else {
                            mSeenStatus.setVisibility(View.GONE);
                            mSeenStatus.setText("");
                        }
                    } else {
                        mSeenStatus.setVisibility(View.GONE);
                        mSeenStatus.setText("");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: " + e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        onlineRef.addValueEventListener(OnlineListener);
        chatText = findViewById(R.id.message_box);
        mSend = findViewById(R.id.message_button);


        chatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ref.child("TYPING").child(mAuth.getUid()).child(intent.getStringExtra("KEY")).setValue("TYPING");
                statusRemover(intent);

                if(s.length() == 0){
                    recorderButton.setVisibility(View.VISIBLE);
                    mSend.setVisibility(View.INVISIBLE);
                }
                else{
                    recorderButton.setVisibility(View.INVISIBLE);
                    mSend.setVisibility(View.VISIBLE);
                    recorderDashboard.setVisibility(View.INVISIBLE);
                    chatText.setVisibility(View.VISIBLE);
                    addImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessage = chatText.getText().toString();
                if (!mMessage.isEmpty()) {
                    messageMap(mMessage, 0);
                    chatText.setText("");
                }
            }
        });

        backButton = findViewById(R.id.back_button_chat);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean checkPermissionForRecorder() {
        int write_external_storage = ContextCompat.checkSelfPermission(ChatListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recorder_permission = ContextCompat.checkSelfPermission(ChatListActivity.this, Manifest.permission.RECORD_AUDIO);

        return write_external_storage == PackageManager.PERMISSION_GRANTED && recorder_permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForRecorder(){
        ActivityCompat.requestPermissions(ChatListActivity.this, new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1000){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupMediaRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(finalRecorderPath);
    }

    private void uploadMediaRecording(String fileName, String filePath){
        messageMap(fileName, 2);
        Uri uriAudio = Uri.fromFile(new File(filePath).getAbsoluteFile());
        final StorageReference ref = storage.getReference().child("recording/" + fileName);
        ref.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ChatListActivity.this, "recording uploaded!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    HashMap messageMap(String Message, int flag) {
        HashMap map = new HashMap();
        Intent intent = getIntent();
        map.put("NAME", USER_NAME);
        map.put("MESSAGE", Message);
        map.put("FROM", mAuth.getUid());
        map.put("TO", intent.getStringExtra("KEY"));
        map.put("TIME", getTime());


        if(flag == 0){
            map.put("TYPE", "MESSAGE");
            writeNotification(Message, USER_NAME);
        }

        if(flag == 1){
            map.put("TYPE", "IMAGE");
            writeNotification("Has sent you an image", USER_NAME);
        }

        if(flag == 2){
            map.put("TYPE", "RECORDING");
            writeNotification("Has sent you a recording", USER_NAME);
        }
        writeToFirebase(map, intent.getStringExtra("KEY"), flag);
        return map;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatListActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    public String getTime() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }


    public String getTimeHM(String string) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(string);
    }

    public String getDateTime() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }

    public void writeToFirebase(final HashMap temp, final String ID, final int flag) {
        if(flag == 0){
            mList.add(temp);
            adapter.notifyDataSetChanged();
        }
        ref.child("NEW MESSAGE").push().setValue(temp, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull final DatabaseReference databaseReference) {
                Log.e(TAG, "onComplete: " + databaseReference);
                ref.child("PROFILE ORDER").child(mAuth.getUid()).child(ID).setValue(getDateTime());
                ref.child("PROFILE ORDER").child(ID).child(mAuth.getUid()).setValue(getDateTime());
                ref.child("UNREAD COUNT").child(mAuth.getUid()).child(ID).setValue(Integer.toString(++mCount));
                mKeyList.add(databaseReference.getKey());
                if(flag == 0){
                    ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(temp.get("MESSAGE").toString());
                    ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue(temp.get("MESSAGE").toString());
                }

                else if(flag == 1){
                    ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("you sent an image");
                    ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue("sent you an image");
                }

                else if(flag == 2){
                    ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("you sent a recording");
                    ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue("sent you a recording");
                }

                ref.child("UNREAD MESSAGE").child(databaseReference.getKey()).setValue(temp);
                temp.put("KEY", databaseReference.getKey());
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (String keys : tokenList)
                            ref.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(keys).child(userKey).child(databaseReference.getKey()).setValue("CONCURRENT KEY");
                    }
                });
                if(flag == 0){
                    mList.remove(mList.size() - 1);
                    mList.add(temp);
                    adapter.notifyDataSetChanged();
                }

                else if(flag == 2){
                    mList.add(temp);
                    adapter.notifyDataSetChanged();
                }

                mKeyList.add(databaseReference.getKey());
                AsyncMessage(temp, databaseReference.getKey());
            }
        });
    }

    public void removeChild(ArrayList<String> keyList) {
        if (!keyList.isEmpty())
            for (String keys : keyList) {
                ref.child("UNREAD MESSAGE").child(keys).removeValue();
                Log.e(TAG, "removeChild: Child has been removed");
            }
    }

    public void statusRemover(final Intent intent) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    TimeUnit.SECONDS.sleep(2);
                    ref.child("TYPING").child(Objects.requireNonNull(mAuth.getUid()))
                            .child(Objects.requireNonNull(intent.getStringExtra("KEY"))).setValue("NOT");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == RESULT_OK){
                Uri image = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    uploadFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void uploadFile(final Bitmap bitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream);
            final byte[] byteArray = stream.toByteArray();
            final String name = mAuth.getUid() + getDateTime() + (new Random().nextInt());
            saveImage(ChatListActivity.this, bitmap, name);
            mList.add(messageMap(name, 1));
            adapter.notifyDataSetChanged();
            final StorageReference ref = storage.getReference().child("images/message/" + name);
            ref.putBytes(byteArray)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }

        catch (Exception e) {
            e.printStackTrace();
        }
   }

    @Override
    protected void onPause() {
        super.onPause();
        //ref.child("ONLINE").child(FirebaseAuth.getInstance().getUid()).setValue("OFFLINE");
        mCount = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: Called in Chat Activity" );

        ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).removeEventListener(UnsentReceiverListener);
        ref.child("UNSENT MESSAGE KEY").child(userKey).child(mAuth.getUid()).removeEventListener(UnsentSelfListener);
        refToken.removeEventListener(TokenListener);
        countRef.removeEventListener(CountListener);
        ref2.removeEventListener(TypingListener);
        onlineRef.removeEventListener(OnlineListener);
        refMain.removeEventListener(MessageReceiverListener);
        ref.child("CONCURRENT USERS").child(mAuth.getUid()).removeEventListener(ConcurrentUserNodeListener);
        ref.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(appToken).child(userKey).removeEventListener(ConcurrentTokensListener);

        adapter.stopAudioPlayer();

        try{
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
        catch (IllegalStateException e){

        }

        try{
            if(mediaRecorder != null){
                mediaRecorder.stop();
                mediaRecorder.release();
            }
        }
        catch (IllegalStateException e){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ref.child("ONLINE").child(FirebaseAuth.getInstance().getUid()).setValue("ONLINE");
        countRef.child(userKey).child(mAuth.getUid()).setValue("0");
        database.getReference().child("TOKENS").child(mAuth.getUid()).setValue(FirebaseInstanceId.getInstance().getToken());

    }

    public void AsyncMessage() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                messageData = database.dao().getMessages();
                mList.clear();
                for (int i = 0; i < messageData.size(); i++) {
                    MessageData data = messageData.get(i);
                    if (data.mFrom.contains(userKey) || data.mTo.contains(userKey) && data.mMessage != null) {
                        HashMap map = new HashMap();
                        Log.e(TAG, "run: " + data.mMessage);
                        Log.e(TAG, "run: " + data.mMessage);
                        if (data.mFrom.contains(userKey))
                            map.put("NAME", userName);

                        if (data.mTo.contains(userKey))
                            map.put("NAME", USER_NAME);

                        map.put("FROM", data.mFrom);
                        map.put("TO", data.mTo);
                        map.put("TIME", data.mTime);
                        map.put("MESSAGE", data.mMessage);
                        map.put("KEY", data.key);
                        map.put("TYPE", data.type);
                        mList.add(map);
                        mKeyList.add(data.key);

                    }
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        // UI code goes here
                        adapter.notifyDataSetChanged();
                        Collections.sort(mList, new Comparator<HashMap>() {
                            @Override
                            public int compare(HashMap o1, HashMap o2) {
                                return o1.get("TIME").toString().compareTo(o2.get("TIME").toString());
                            }
                        });
                        Log.e(TAG, "run: Adapter called");
                        listView = findViewById(R.id.chat_list_view);
                        listView.setAdapter(adapter);

                        refMain.addValueEventListener(MessageReceiverListener);

                    }
                });

            }
        };
        thread.start();
    }

    void AsyncMessage(final HashMap map, final String key) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                    MessageData dataObject = new MessageData(key, map.get("FROM").toString(), map.get("TO").toString(), map.get("TIME").toString(), map.get("MESSAGE").toString(), map.get("TYPE").toString());
                    database.dao().InsertMessage(dataObject);
                    Log.e(TAG, "run: Message Added");
                } catch (Exception e) {

                }
            }

        };
        thread.start();
    }

    public void writeNotification(String Message, String Name) {
        if (token != null) {
            content = new NotificationContent();
            notificationBody = new NotificationBody();

            content.setBody(Message);
            content.setTitle(Name + ": " + userName);

            notificationBody.setNotificationContent(content);
            notificationBody.setToken(token);

            apiInterface = RetrofitClient.getClient().create(APIInterface.class);
            retrofit2.Call<ResponseBody> responseBodyCall = apiInterface.sendNotification(notificationBody);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e(TAG, "onResponse: " + call.toString() + " Response: " + response.toString() + " Body " + response.body() + "Code" + response.code());
                    if (response.code() == 400) {
                        try {
                            Log.e(TAG, "onResponse: Error Body " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            String str = response.errorBody().string();
                            JSONObject jObjError = new JSONObject(str);
                            Toast.makeText(ChatListActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException | IOException e) {
                            Log.e(TAG, "onResponse: JSON Exception" + e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        }
    }

    int GetListPositionForKey(String key) {
        int i = 0;
        for (HashMap map : mList) {
            if (map.get("KEY").toString().contains(key)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    void FirebaseReferenceInitializer() {
        //Unsent from
        UnsentReceiverListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (final DataSnapshot snap : snapshot.getChildren()) {
                        int x = GetListPositionForKey(snap.getKey());
                        if (x != -1) {
                            mList.remove(x);
                            mKeyList.remove(snap.getKey());
                            adapter.notifyDataSetChanged();
                            Log.e(TAG, "onDataChange: Removable keys" + snap.getKey());
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                                        database.dao().deleteTuple(snap.getKey());
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        }

                    }
                }
                ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).setValue("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Unsent to
        UnsentSelfListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (final DataSnapshot snap : snapshot.getChildren()) {
                        Log.e(TAG, "onDataChange: Removable keys" + snap.getKey());
                        int x = GetListPositionForKey(snap.getKey());
                        if (x != -1) {
                            mList.remove(x);
                            mKeyList.remove(snap.getKey());
                            adapter.notifyDataSetChanged();
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                                        database.dao().deleteTuple(snap.getKey());
                                    } catch (Exception e) {

                                    }
                                }

                            });
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Token
        TokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                token = snapshot.getValue().toString();
                Log.e(TAG, "Token: " + appToken);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Name
        CountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    mCount = Integer.parseInt(snapshot.child(mAuth.getUid()).child(Objects.requireNonNull(userKey)).getValue().toString());
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Typing listener
        TypingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        typingStatus = snapshot.child("TYPING").child(Objects.requireNonNull(userKey))
                                .child(Objects.requireNonNull(mAuth.getUid())).getValue().toString();
                        if (typingStatus.contains("TYPING")) {
                            mTypingStatus.setVisibility(View.VISIBLE);
                            mSeenStatus.setVisibility(View.GONE);
                            mTypingStatus.setText("Typing...");
                        } else {
                            mTypingStatus.setVisibility(View.GONE);
                            mSeenStatus.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //Online Listener
        OnlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String mStatus = snapshot.child("ONLINE").child(Objects.requireNonNull(userKey)).getValue().toString();
                    if (mStatus.contains("ONLINE")) {
                        mOnlineStatus.setVisibility(View.VISIBLE);
                        mOnlineStatus.setText("Online");
                    } else {
                        mOnlineStatus.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //RefMain Message Listener
        MessageReceiverListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        removeKey.clear();
                        for (final DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) &&
                                    snap.child("FROM").getValue().toString().contains(userKey)) {
                                if (!mKeyList.contains(snap.getKey())) {
                                    HashMap map = new HashMap();
                                    map.put("KEY", snap.getKey());
                                    map.put("NAME", userName);
                                    map.put("MESSAGE", snap.child("MESSAGE").getValue());
                                    map.put("FROM", snap.child("FROM").getValue());
                                    map.put("TO", snap.child("TO").getValue());
                                    map.put("TIME", snap.child("TIME").getValue().toString());
                                    map.put("TYPE", snap.child("TYPE").getValue().toString());
                                    mList.add(map);
                                    mKeyList.add(snap.getKey());

                                    Collections.sort(mList, new Comparator<HashMap>() {
                                        @Override
                                        public int compare(HashMap o1, HashMap o2) {
                                            return o1.get("TIME").toString().compareTo(o2.get("TIME").toString());
                                        }
                                    });

                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (String keys : tokenList)
                                                ref.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(keys).child(userKey).child(snap.getKey()).setValue("CONCURRENT KEY");
                                        }
                                    });

                                    adapter.notifyDataSetChanged();

                                    if(map.get("TYPE").toString().contains("MESSAGE"))
                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(mList.get(mList.size() - 1).get("MESSAGE").toString());

                                    else if(map.get("TYPE").toString().contains("IMAGE"))
                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("sent you an image");

                                    Log.e(TAG, "onDataChange: Senders Message" + snap.getKey());
                                    AsyncMessage(map, snap.getKey());
                                    removeKey.add(snap.getKey());
                                }
                            }
                        }
                        countRef.child(userKey).child(mAuth.getUid()).setValue("0");
                        removeChild(removeKey);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ConcurrentUserNodeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap!=null) {
                        Log.e(TAG, "onDataChange: Token " + snap.getKey());
                        if(!Objects.requireNonNull(snap.getKey()).contains(appToken)){
                            tokenList.add(snap.getKey());
                            Log.e(TAG, "onDataChange: Added token: " + snap.getKey() );
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ConcurrentTokensListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                concurrentMessageKeys.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Log.e(TAG, "onDataChange: Keys" + snap.getKey());
                    Toast.makeText(ChatListActivity.this, snap.getKey(), Toast.LENGTH_SHORT).show();
                    concurrentMessageKeys.add(snap.getKey());
                }
                ConcurrentMessageRetriever(concurrentMessageKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ConcurrentMessageReceiverListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final HashMap map = new HashMap();
                map.put("NAME", USER_NAME);
                map.put("KEY", concurrentFilterKey);
                map.put("FROM", snapshot.child("FROM").getValue().toString());
                map.put("TO", snapshot.child("TO").getValue().toString());
                map.put("TIME", snapshot.child("TIME").getValue().toString());
                map.put("MESSAGE", snapshot.child("MESSAGE").getValue().toString());
                map.put("TYPE", snapshot.child("TYPE").getValue().toString());
                mList.add(map);
                mKeyList.add(concurrentFilterKey);

                Collections.sort(mList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        return o1.get("TIME").toString().compareTo(o2.get("TIME").toString());
                    }
                });

                adapter.notifyDataSetChanged();

                Log.e(TAG, "onDataChange: AsyncSingleValueListener");
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                            MessageData dataObject = new MessageData(concurrentFilterKey,
                                    snapshot.child("FROM").getValue().toString(),
                                    snapshot.child("TO").getValue().toString(),
                                    snapshot.child("TIME").getValue().toString(),
                                    snapshot.child("MESSAGE").getValue().toString(),
                                    snapshot.child("TYPE").getValue().toString());
                            database.dao().InsertMessage(dataObject);
                            Log.e(TAG, "run: Added to DB");
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: Database error" + e.toString());
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }

    void ConcurrentMessageRetriever(final ArrayList<String> concurrentMessageKeys){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for(String mKey : concurrentMessageKeys){
                    concurrentFilterKey = mKey;
                    if(!mKeyList.contains(mKey)) {
                        ref.child("NEW MESSAGE").child(mKey).addListenerForSingleValueEvent(ConcurrentMessageReceiverListener);
                    }
                    ref.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(appToken).child(userKey).child(mKey).removeValue();
                }

            }
        });
    }

    void saveImage(Context context, Bitmap bitmap, String name){
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

