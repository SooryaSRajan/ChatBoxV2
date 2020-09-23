package com.example.chatbox.list_adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.chatbox.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class profileListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<HashMap> list;

    public profileListAdapter(Context context, List<HashMap> list){
        this.context = context;
        this.list = list;
         inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        if(list!=null) {
            return list.get(position).containsKey("NAME");
        }
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView textView = null;
        try {
            convertView = inflater.inflate(R.layout.profile_list_view, null);
            textView = convertView.findViewById(R.id.user_name);
            textView.setText(list.get(position).get("NAME").toString());
        }
        catch (Exception e){
            Log.e("Adapter Profile", "getView: " + e.toString() );
        }

        try{
                TextView mTextView = convertView.findViewById(R.id.message_count);
                mTextView.setText(list.get(position).get("COUNT").toString());
                if(textView!=null){
                    textView.setTypeface(null, Typeface.BOLD_ITALIC);
                }

        }
        catch (Exception e){
            TextView mTextView = convertView.findViewById(R.id.message_count);
            mTextView.setVisibility(View.GONE);

            if(textView!=null){
                textView.setTypeface(null, Typeface.NORMAL);
            }
        }
        TextView status = convertView.findViewById(R.id.online_status_color);
        status.setVisibility(View.GONE);
        TextView lastMessageTextView = convertView.findViewById(R.id.last_message);

        try {
            String mStatus = list.get(position).get("ONLINE").toString();

            if(mStatus.contains("ONLINE")) {
                status.setVisibility(View.VISIBLE);
                status.setBackground(ContextCompat.getDrawable(context, R.drawable.online));
            }

            else if(mStatus.contains("OFFLINE")){
                status.setVisibility(View.VISIBLE);
                status.setBackground(ContextCompat.getDrawable(context, R.drawable.offline));
            }

        }
        catch (Exception e){
            Log.e(TAG, "getView: " + e.toString() );
        }

        try {
            String lastMessage = list.get(position).get("LAST MESSAGE").toString();
            lastMessageTextView.setText(lastMessage);
        }
        catch (Exception e){

        }

        try{
            //ImageButton button = convertView.findViewById(R.id.profile_picture);
            /*button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, list.get(position).get("NAME").toString(), Toast.LENGTH_SHORT).show();
                }
            });
            */

        }
        catch (Exception e){
            Log.e(TAG, "getView: " + e.toString() );
        }
        TextView mTime = convertView.findViewById(R.id.user_list_time);

        try{
            String time = list.get(position).get("DATE").toString();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = format.parse(time);
                Date currentTime = Calendar.getInstance().getTime();

                format = new SimpleDateFormat("yyyy-MM-dd");
                String chatDate = format.format(date);
                String curDate = format.format(currentTime);
                if (chatDate.contains(curDate)) {
                    format = new SimpleDateFormat("hh:mm aa");
                    time = format.format(date);
                    mTime.setText(time);
                    Log.e(TAG, "Adapter Time Matches");

                } else {
                    format = new SimpleDateFormat("dd-MM-yyyy");
                    time = format.format(date);
                    Log.e(TAG, "Time doesnt Match");
                    mTime.setText(time);
                }
            } catch (Exception e) {
                Log.e(TAG, "Chat Adapter Time Exception: " + e.toString());
            }
        }
        catch (Exception e){

        }

        return  convertView;

    }
}
