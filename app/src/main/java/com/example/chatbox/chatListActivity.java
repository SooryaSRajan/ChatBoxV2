package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatbox.list_adapters.chatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.chatbox.CONSTANTS.USER_NAME;

public class chatListActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageButton backButton;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference(), ref2 = database.getReference();
    private ValueEventListener eventListener, eventListener2;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView listView;
    ArrayList<HashMap> mList = new ArrayList<>();
    private static chatAdapter adapter;
    EditText chatText;
    ImageButton mSend;
    String mMessage;
    String TAG = "ChatListActivity";
    ArrayList<String> removeKey = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        final Intent intent = getIntent();

        toolbar = findViewById(R.id.tool_bar_chat);
        toolbar.setTitle("");
        TextView toolBarName = findViewById(R.id.toolbar_name);
        toolBarName.setText(intent.getStringExtra("NAME"));
        setSupportActionBar(toolbar);
        ref.child("MESSAGE POOL").removeValue();

        eventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mList.clear();
                if (snapshot.exists()) {
                    for(DataSnapshot snap : snapshot.child("NEW MESSAGE").getChildren()) {
                        if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) &&
                                snap.child("FROM").getValue().toString().contains(intent.getStringExtra("KEY"))){
                            HashMap map = new HashMap();
                            map.put("NAME", intent.getStringExtra("NAME"));
                            map.put("MESSAGE", snap.child("MESSAGE").getValue());
                            map.put("FROM", snap.child("FROM").getValue());
                            map.put("TO", snap.child("TO").getValue());
                            map.put("TIME", snap.child("TIME").getValue());
                            mList.add(map);
                            Log.e(TAG, "onDataChange: Senders Message" );
                        }

                        if (snap.child("TO").getValue().toString().contains(intent.getStringExtra("KEY")) &&
                                snap.child("FROM").getValue().toString().contains(mAuth.getUid())){
                            HashMap map = new HashMap();

                            map.put("NAME", USER_NAME);
                            map.put("MESSAGE", snap.child("MESSAGE").getValue());
                            map.put("FROM", snap.child("FROM").getValue());
                            map.put("TO", snap.child("TO").getValue());
                            map.put("TIME", snap.child("TIME").getValue());
                            mList.add(map);
                            Log.e(TAG, "onDataChange: Recievers Message" );
                        }
                    }
                    adapter = new chatAdapter(chatListActivity.this, mList);
                    listView = findViewById(R.id.chat_list_view);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    scrollMyListViewToBottom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(eventListener);
/*
        eventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.child("NEW MESSAGE").getChildren()) {
                        if (snap.child("TO").getValue().toString().contains(mAuth.getUid()) ||
                                snap.child("FROM").getValue().toString().contains(mAuth.getUid())) {
                            HashMap map = new HashMap();
                            map.put("MESSAGE", snap.child("MESSAGE").getValue());
                            map.put("FROM", snap.child("FROM").getValue());
                            map.put("TO", snap.child("TO").getValue());
                            map.put("TIME", snap.child("TIME").getValue());
                            removeKey.add(snap.getKey());
                            Log.e(TAG, "onDataChange: Message Key: " + snap.getKey());
                            mList.add(map);
                            writeFinal(map);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

     //   ref2.addValueEventListener(eventListener2);
*/
        chatText = findViewById(R.id.message_box);
        mSend = findViewById(R.id.message_button);

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
     // scrollMyListViewToBottom();
    }

    void messageMap(String Message){
        HashMap map = new HashMap();
        Intent intent = getIntent();
        map.put("MESSAGE", Message);
        map.put("FROM", mAuth.getUid());
        map.put("TO", intent.getStringExtra("KEY"));
        map.put("TIME",getTime());
        mList.add(map);

        writeToFirebase(map);
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


    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
               // listView = findViewById(R.id.chat_list_view);
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    public void writeToFirebase(HashMap temp) {
        ref.child("NEW MESSAGE").push().setValue(temp, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(chatListActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onComplete: Message Sent" );
                }
            }

        });
       /* adapter = new chatAdapter(chatListActivity.this, mList);
        listView = findViewById(R.id.chat_list_view);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
*/
    }

    public void writeFinal(HashMap temp){
        ref.child("MESSAGE POOL").push().setValue(temp, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "onComplete: Child has been added to final list");
                }
            }
        });
    }

    public void removeChild(ArrayList<String> keyList){
       if(!keyList.isEmpty())
        for(String keys : keyList){
            ref.child("NEW MESSAGE").child(keys).removeValue();
            Log.e(TAG, "removeChild: Child has been removed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  ref.removeEventListener(eventListener);
    }

}

