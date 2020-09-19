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
        return  convertView;

    }
}
