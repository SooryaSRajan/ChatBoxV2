package com.example.chatbox.list_adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbox.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class ChatAdapter extends BaseAdapter {

    private ArrayList<HashMap> chatList;
    private Activity activity;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ChatAdapter(Activity activity, ArrayList<HashMap> chatList){
        this.chatList = chatList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final HashMap map = chatList.get(position);

        LayoutInflater inflater = activity.getLayoutInflater();
        if(map.get("FROM").toString().contains(mAuth.getUid()))
            convertView = inflater.inflate(R.layout.sender, null);

        else
            convertView = inflater.inflate(R.layout.reciever, null);

        TextView mName = convertView.findViewById(R.id.user_name_list);
        TextView mMessage = convertView.findViewById(R.id.chat_text_list);
        TextView mTime = convertView.findViewById(R.id.text_time_list);


        /***Date And Time Formatter For List View Chat**/
        String time = map.get("TIME").toString();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(time);
            Date currentTime = Calendar.getInstance().getTime();

            format = new SimpleDateFormat("yyyy-MM-dd");
            String chatDate = format.format(date);
            String curDate = format.format(currentTime);

            if(chatDate.contains(curDate)){
                format = new SimpleDateFormat("hh:mm aa");
                time = format.format(date);
                Log.e(TAG, "Adapter Time Matches");
            }

            else{
                format = new SimpleDateFormat("dd-MM-yyyy HH:mm aa");
                time = format.format(date);
                Log.e(TAG, "Time doesnt Match" );
            }
        }

        catch (Exception e){
            Log.e(TAG, "Chat Adapter Time Exception: " + e.toString() );
        }

        try {
            mName.setText(map.get("NAME").toString());
        }
        catch (Exception e){
            Log.e(TAG, "getView:  Name" + e.toString() );
        }

        try {
            mMessage.setText(map.get("MESSAGE").toString());
            mTime.setText(time);
        }
        catch (Exception e){

        }

        return convertView;
    }
}
