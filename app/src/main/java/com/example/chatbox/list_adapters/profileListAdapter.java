package com.example.chatbox.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            convertView = inflater.inflate(R.layout.profile_list_view, null);
            TextView textView = convertView.findViewById(R.id.user_name);
            textView.setText(list.get(position).get("NAME").toString());
        }
        catch (Exception e){
            Log.e("Adapter Profile", "getView: " + e.toString() );
        }

        try{
                TextView textView = convertView.findViewById(R.id.message_count);
                textView.setText(list.get(position).get("COUNT").toString());
        }
        catch (Exception e){
            TextView textView = convertView.findViewById(R.id.message_count);
            textView.setVisibility(View.GONE);
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
        return  convertView;

    }
}
