package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatbox.FCMNotification.APIService;
import com.example.chatbox.FCMNotification.Data;
import com.example.chatbox.FCMNotification.NotificationClient;
import com.example.chatbox.FCMNotification.Sender;
import com.example.chatbox.MessageDatabase.MessageData;
import com.example.chatbox.MessageDatabase.MessageDatabase;
import com.example.chatbox.list_adapters.chatAdapter;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.chatbox.CONSTANTS.USER_NAME;

public class chatListActivity extends AppCompatActivity {

    Toolbar toolbar;
    List<MessageData> messageData = null;
    ImageButton backButton;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference countRef = database.getReference().child("UNREAD COUNT"), ref = database.getReference(),
            ref2 = database.getReference(), onlineRef = database.getReference(), refMain = database.getReference().child("UNREAD MESSAGE");

    private ValueEventListener eventListener, eventListener2, onlineListener;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView listView;
    ArrayList<HashMap> mList = new ArrayList<>();
    private static chatAdapter adapter;
    EditText chatText;
    ImageButton mSend;
    String mMessage;
    String TAG = "ChatListActivity";
    ArrayList<String> removeKey = new ArrayList<>();
    String typingStatus;
    TextView mTypingStatus, mOnlineStatus, mSeenStatus;
    ImageButton addImage;
    String userKey, userName;
    int mCount = 0;
    int otherCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        final Intent intent = getIntent();
        AsyncMessage();
        userKey = intent.getStringExtra("KEY");
        userName = intent.getStringExtra("NAME");
        ref.child("TYPING").child(Objects.requireNonNull(intent.getStringExtra("KEY")))
                .child(Objects.requireNonNull(mAuth.getUid())).setValue("NOT");

        ValueEventListener countListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    mCount = Integer.parseInt(snapshot.child(mAuth.getUid()).child(intent.getStringExtra("KEY")).getValue().toString());
                }
                catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        countRef.addValueEventListener(countListener);

        toolbar = findViewById(R.id.tool_bar_chat);
        toolbar.setTitle("");
        TextView toolBarName = findViewById(R.id.toolbar_name);
        toolBarName.setText(intent.getStringExtra("NAME"));
        setSupportActionBar(toolbar);
        addImage = findViewById(R.id.add_resource);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);
            }
        });

        mOnlineStatus = findViewById(R.id.online_status);
        mTypingStatus = findViewById(R.id.typing_status);
        mSeenStatus = findViewById(R.id.seen_status);
        mTypingStatus.setVisibility(View.GONE);
        mSeenStatus.setVisibility(View.GONE);

        eventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                      try{
                        typingStatus = snapshot.child("TYPING").child(Objects.requireNonNull(intent.getStringExtra("KEY")))
                                .child(Objects.requireNonNull(mAuth.getUid())).getValue().toString();
                        if (typingStatus.contains("TYPING")) {
                            mTypingStatus.setVisibility(View.VISIBLE);
                            mSeenStatus.setVisibility(View.GONE);
                            mTypingStatus.setText("Typing...");
                        }
                        else{
                            mTypingStatus.setVisibility(View.GONE);
                            mSeenStatus.setVisibility(View.VISIBLE);
                        }
                      }
                      catch (Exception e){
                          Log.e(TAG, "onDataChange: " + e.toString() );
                      }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref2.addValueEventListener(eventListener2);

        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String value = snapshot.child(userKey).child(mAuth.getUid()).getValue().toString();
                    String key = mList.get(mList.size()-1).get("FROM").toString();
                    if (value.contains("0")) {
                       if (key.contains(mAuth.getUid())) {
                            mTypingStatus.setVisibility(View.GONE);
                            mSeenStatus.setVisibility(View.VISIBLE);
                            mSeenStatus.setText("seen");
                        }
                       else{
                           mSeenStatus.setVisibility(View.GONE);
                           mSeenStatus.setText("");
                       }
                    }
                    else {
                        mSeenStatus.setVisibility(View.GONE);
                        mSeenStatus.setText("");
                    }
                }
                catch (Exception e){
                    Log.e(TAG, "onDataChange: " + e.toString() );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        onlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String mStatus = snapshot.child("ONLINE").child(Objects.requireNonNull(intent.getStringExtra("KEY"))).getValue().toString();
                    if (mStatus.contains("ONLINE")) {
                        mOnlineStatus.setVisibility(View.VISIBLE);
                        mOnlineStatus.setText("Online");
                    }
                    else{
                        mOnlineStatus.setVisibility(View.GONE);
                    }
                }
                catch (Exception ignored){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        onlineRef.addValueEventListener(onlineListener);
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
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessage = chatText.getText().toString();
                if(!mMessage.isEmpty()){
                    messageMap(mMessage);
                    chatText.setText("");
                }
            }
        });


        backButton = findViewById(R.id.back_button_chat);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chatListActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    void messageMap(String Message){
        HashMap map = new HashMap();
        Intent intent = getIntent();
        map.put("NAME", USER_NAME);
        map.put("MESSAGE", Message);
        map.put("FROM", mAuth.getUid());
        map.put("TO", intent.getStringExtra("KEY"));
        map.put("TIME",getTime());

        mList.add(map);
        adapter = new chatAdapter(chatListActivity.this, mList);
        listView = findViewById(R.id.chat_list_view);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        writeToFirebase(map, intent.getStringExtra("KEY"));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(chatListActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    public String getTime(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(currentTime);
    }

    public String getDateTime(){
        Date currentTime = Calendar.getInstance().getTime();
        return currentTime.toString();
    }

    public void writeToFirebase(final HashMap temp, final String ID) {
        ref.child("NEW MESSAGE").push().setValue(temp, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                ref.child("PROFILE ORDER").child(mAuth.getUid()).child(ID).setValue(getDateTime());
                ref.child("PROFILE ORDER").child(ID).child(mAuth.getUid()).setValue(getDateTime());
                ref.child("UNREAD COUNT").child(mAuth.getUid()).child(ID).setValue(Integer.toString(++mCount));
                AsyncMessage(temp, databaseReference.getKey());
            }

        });
        ref.child("UNREAD MESSAGE").push().setValue(temp);
    }

    public void removeChild(ArrayList<String> keyList){
       if(!keyList.isEmpty())
        for(String keys : keyList){
            ref.child("UNREAD MESSAGE").child(keys).removeValue();
            Log.e(TAG, "removeChild: Child has been removed");
        }
    }

    public void statusRemover(final Intent intent){
        Thread thread = new Thread(){
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            uploadFile(bitmap);
        }
    }

    private void uploadFile(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        ByteArrayOutputStream ByteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, ByteStream);
        byte[] data = ByteStream.toByteArray();

        //storageRef.putBytes()
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCount = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refMain.removeEventListener(eventListener);

        Intent broadcastIntent = new Intent(chatListActivity.this, RestartServiceBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 123, broadcastIntent, 0);
        long startTime = System.currentTimeMillis();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);

        Intent intent = new Intent("ReceiveNotificationBroadcast");
        sendBroadcast(intent);
        mCount = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        countRef.child(userKey).child(mAuth.getUid()).setValue("0");

    }

    public void AsyncMessage(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                MessageDatabase database = MessageDatabase.getInstance(chatListActivity.this);
                messageData = database.dao().getMessages();

                for(int i = 0; i<messageData.size(); i++) {
                    MessageData data = messageData.get(i);
                    if (data.mFrom.contains(userKey) || data.mTo.contains(userKey)) {
                        HashMap map = new HashMap();
                        Log.e(TAG, "run: " + data.mMessage);
                        Log.e(TAG, "run: " + data.mMessage);
                        if(data.mFrom.contains(userKey))
                        map.put("NAME", userName);

                        if(data.mTo.contains(userKey))
                        map.put("NAME", USER_NAME);

                        map.put("FROM", data.mFrom);
                        map.put("TO", data.mTo);
                        map.put("TIME", data.mTime);
                        map.put("MESSAGE", data.mMessage);
                        mList.add(map);
                    }
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        // UI code goes here
                        Log.e(TAG, "run: Adapter called");
                        adapter = new chatAdapter(chatListActivity.this, mList);
                        listView = findViewById(R.id.chat_list_view);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        eventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    try {
                                        removeKey.clear();
                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) &&
                                                    snap.child("FROM").getValue().toString().contains(userKey)) {
                                                HashMap map = new HashMap();
                                                map.put("NAME", userName);
                                                map.put("MESSAGE", snap.child("MESSAGE").getValue());
                                                map.put("FROM", snap.child("FROM").getValue());
                                                map.put("TO", snap.child("TO").getValue());
                                                map.put("TIME", snap.child("TIME").getValue());
                                                mList.add(map);
                                                Log.e(TAG, "onDataChange: Senders Message" + snap.getKey());
                                                AsyncMessage(map, snap.getKey());
                                                removeKey.add(snap.getKey());
                                                countRef.child(userKey).child(mAuth.getUid()).setValue("0");
                                            }
                                        }
                                        removeChild(removeKey);
                                    }
                                    catch (Exception e){
                                        Log.e(TAG, "onDataChange: " + e.toString() );
                                    }

                                    adapter = new chatAdapter(chatListActivity.this, mList);
                                    listView = findViewById(R.id.chat_list_view);
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                     refMain.addValueEventListener(eventListener);

                    }
                });

            }
        };
        thread.start();
    }

    void AsyncMessage(final HashMap map, final String key){
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    MessageDatabase database = MessageDatabase.getInstance(chatListActivity.this);
                    MessageData dataObject = new MessageData(key, map.get("FROM").toString(), map.get("TO").toString(), map.get("TIME").toString(), map.get("MESSAGE").toString());
                    database.dao().InsertMessage(dataObject);
                    Log.e(TAG, "run: Message Added");
                }
            catch (Exception e){

            }
            }

        };
        thread.start();
    }
}

