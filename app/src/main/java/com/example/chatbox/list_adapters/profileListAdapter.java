package com.example.chatbox.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.chatbox.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        convertView = inflater.inflate(R.layout.profile_list_view, null);
        TextView textView =  convertView.findViewById(R.id.user_name);
        textView.setText(list.get(position).get("NAME").toString());

        return  convertView;

    }
}
