package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
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
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.chatbox.Constants.USER_NAME;

public class ChatListActivity extends AppCompatActivity {

    Toolbar toolbar;
    List<MessageData> messageData = null;
    ImageButton backButton;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference countRef = database.getReference().child("UNREAD COUNT"), ref = database.getReference(),
            ref2 = database.getReference(), onlineRef = database.getReference(), refMain = database.getReference().child("UNREAD MESSAGE"),
            refToken;

    private ValueEventListener eventListener, eventListener2, onlineListener;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView listView;
    ArrayList<HashMap> mList = new ArrayList<>();
    private static ChatAdapter adapter;
    EditText chatText;
    ImageButton mSend;
    String mMessage;
    String TAG = "ChatListActivity";
    ArrayList<String> removeKey = new ArrayList<>();
    String typingStatus;
    TextView mTypingStatus, mOnlineStatus, mSeenStatus;
    ImageButton addImage;
    String userKey, userName, token;
    int mCount = 0;
    int otherCount;
    NotificationContent content;
    NotificationBody notificationBody;
    APIInterface apiInterface;
    int onLongClickPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        final Intent intent = getIntent();

        userKey = intent.getStringExtra("KEY");
        userName = intent.getStringExtra("NAME");

        Log.e(TAG, "onCreate: " + USER_NAME + userName + userKey);
        final ArrayList<String> messageDeleteList = new ArrayList<>();

            ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).setValue("0");
                    }

                    else{
                        for(DataSnapshot snap : snapshot.getChildren()){
                            Log.e(TAG, "onDataChange: Removable keys" + snap.getKey());
                            messageDeleteList.add(snap.getKey());
                        }
                        ref.child("UNSENT MESSAGE KEY").child(mAuth.getUid()).child(userKey).setValue("0");
                        asyncRemoveMessage(messageDeleteList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        try {
            refToken = database.getReference().child("TOKENS").child(userKey);
            database.getReference().child("TOKENS").child(mAuth.getUid()).setValue(FirebaseInstanceId.getInstance().getToken());

            refToken.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot!=null)
                       if(snapshot.exists())
                    token = snapshot.getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        catch (Exception e){
            Log.e(TAG, "onCreate: Token "+e.toString() );
            Toast.makeText(this, userKey, Toast.LENGTH_SHORT).show();
        }

        AsyncMessage();
         ref.child("TYPING").child(Objects.requireNonNull(intent.getStringExtra("KEY")))
                .child(Objects.requireNonNull(mAuth.getUid())).setValue("NOT");

        ValueEventListener countListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    mCount = Integer.parseInt(snapshot.child(mAuth.getUid()).child(Objects.requireNonNull(intent.getStringExtra("KEY"))).getValue().toString());
                }
                catch (Exception ignored){

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

        listView = findViewById(R.id.chat_list_view);

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

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    PopupMenu popupMenu= new PopupMenu(ChatListActivity.this, view);
                        popupMenu.getMenuInflater().inflate(R.menu.chat_context_menu, popupMenu.getMenu());

                    try {
                        if (mList.get(position).get("FROM").toString().contains(mAuth.getUid())) {
                            onLongClickPosition = position;
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    final String keys = mList.get(onLongClickPosition).get("KEY").toString();

                                    if(item.getItemId() == R.id.unsend_message_menu){
                                        Log.e(TAG, "onClick: " + keys );
                                        ref.child("NEW MESSAGE").child(keys).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference refer) {
                                                refMain.child(keys).removeValue();
                                                mList.remove(onLongClickPosition);
                                                adapter.notifyDataSetChanged();
                                                asyncRemoveMessage(keys);
                                                ref.child("UNSENT MESSAGE KEY").child(userKey).child(mAuth.getUid()).child(keys).setValue("ADDED");

                                                Log.e(TAG, "onComplete: " + mList.size() + " " + onLongClickPosition);

                                                try {
                                                    if (mList.size() == onLongClickPosition) {
                                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(mList.get(onLongClickPosition - 1).get("MESSAGE").toString());
                                                        ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue(mList.get(onLongClickPosition - 1).get("MESSAGE").toString());
                                                    }
                                                }
                                                catch (Exception e) {
                                                    if (mList.size() == 0) {
                                                        ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue("");
                                                        ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue("");
                                                    }
                                                }

                                            }
                                        });
                                    }
                                    if(item.getItemId() == R.id.remove_local_message){
                                        asyncRemoveMessage(keys);
                                    }
                                    return true;
                                }
                            });
                            popupMenu.show();
                        //    alertDialog.show();
                        }
                    } catch (Exception e) {

                    }
                    return false;
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
                Intent intent = new Intent(ChatListActivity.this, HomePageActivity.class);
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


        writeToFirebase(map, intent.getStringExtra("KEY"));
        writeNotification(Message, USER_NAME);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatListActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    public String getTime(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }


    public String getTimeHM(String string){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(string);
    }

    public String getDateTime(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }

    public void writeToFirebase(final HashMap temp, final String ID) {
        mList.add(temp);
        adapter.notifyDataSetChanged();
        ref.child("NEW MESSAGE").push().setValue(temp, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.e(TAG, "onComplete: " + databaseReference);
                ref.child("PROFILE ORDER").child(mAuth.getUid()).child(ID).setValue(getDateTime());
                ref.child("PROFILE ORDER").child(ID).child(mAuth.getUid()).setValue(getDateTime());
                ref.child("UNREAD COUNT").child(mAuth.getUid()).child(ID).setValue(Integer.toString(++mCount));
                ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(temp.get("MESSAGE").toString());
                ref.child("LAST MESSAGE").child(userKey).child(mAuth.getUid()).setValue(temp.get("MESSAGE").toString());
                ref.child("UNREAD MESSAGE").child(databaseReference.getKey()).setValue(temp);
                temp.put("KEY", databaseReference.getKey());
                mList.remove(mList.size()-1);
                mList.add(temp);
                adapter.notifyDataSetChanged();
                AsyncMessage(temp, databaseReference.getKey());
            }

        });
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
        ref.child("ONLINE").child(FirebaseAuth.getInstance().getUid()).setValue("OFFLINE");
        mCount = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refMain.removeEventListener(eventListener);

        mCount = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ref.child("ONLINE").child(FirebaseAuth.getInstance().getUid()).setValue("ONLINE");
        countRef.child(userKey).child(mAuth.getUid()).setValue("0");
        database.getReference().child("TOKENS").child(mAuth.getUid()).setValue(FirebaseInstanceId.getInstance().getToken());

    }

    public void AsyncMessage(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                messageData = database.dao().getMessages();

                mList.clear();
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
                        map.put("KEY", data.key);
                        mList.add(map);

                    }
                }

                Collections.sort(mList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        return o1.get("TIME").toString().compareTo(o2.get("TIME").toString());
                    }
                });

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        // UI code goes here
                        Log.e(TAG, "run: Adapter called");
                        adapter = new ChatAdapter(ChatListActivity.this, mList);
                        listView = findViewById(R.id.chat_list_view);
                        listView.setAdapter(adapter);

                        adapter.notifyDataSetChanged();

                        eventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    try {
                                        removeKey.clear();
                                        countRef.child(userKey).child(mAuth.getUid()).setValue("0");

                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) &&
                                                    snap.child("FROM").getValue().toString().contains(userKey)) {
                                                HashMap map = new HashMap();
                                                map.put("NAME", userName);
                                                map.put("MESSAGE", snap.child("MESSAGE").getValue());
                                                map.put("FROM", snap.child("FROM").getValue());
                                                map.put("TO", snap.child("TO").getValue());
                                                map.put("TIME", snap.child("TIME").getValue().toString());
                                                mList.add(map);
                                                adapter.notifyDataSetChanged();
                                                ref.child("LAST MESSAGE").child(mAuth.getUid()).child(userKey).setValue(mList.get(mList.size()-1).get("MESSAGE").toString());
                                                Log.e(TAG, "onDataChange: Senders Message" + snap.getKey());
                                                AsyncMessage(map, snap.getKey());
                                                removeKey.add(snap.getKey());
                                            }
                                        }
                                        removeChild(removeKey);
                                    }
                                    catch (Exception e){
                                        Log.e(TAG, "onDataChange: " + e.toString() );
                                    }
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
                    MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
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

    void asyncRemoveMessage(final String key){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                database.dao().deleteTuple(key);
            }
        });
    }

    void asyncRemoveMessage(final ArrayList<String> key){
        final int[] flag = {0};
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                for(String keys : key){
                    database.dao().deleteTuple(keys);
                    flag[0]++;
                }
                if(flag[0]!=0){{
                 AsyncUpdater();
                }}
            }
        });
    }

    void AsyncUpdater() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                MessageDatabase database = MessageDatabase.getInstance(ChatListActivity.this);
                messageData = database.dao().getMessages();

                mList.clear();
                for (int i = 0; i < messageData.size(); i++) {
                    MessageData data = messageData.get(i);
                    if (data.mFrom.contains(userKey) || data.mTo.contains(userKey)) {
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
                        mList.add(map);

                    }
                }

                Collections.sort(mList, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        return o1.get("TIME").toString().compareTo(o2.get("TIME").toString());
                    }
                });

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        // UI code goes here
                        Log.e(TAG, "run: Adapter called");
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };
        thread.start();
    }

}

