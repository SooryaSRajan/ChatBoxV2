package com.example.chatbox.list_adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.chatbox.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class ChatAdapter extends BaseAdapter {

    FirebaseStorage storage;
    private ArrayList<HashMap> chatList;
    StorageReference mDataRef;
    private Activity activity;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ChatAdapter(Activity activity, ArrayList<HashMap> chatList){
        this.chatList = chatList;
        this.activity = activity;

        storage = FirebaseStorage.getInstance();
        mDataRef = storage.getReferenceFromUrl("gs://chat-box-v2.appspot.com");
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

        if(map.get("TYPE").toString().contains("MESSAGE")) {

            if (map.get("FROM").toString().contains(mAuth.getUid()))
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

                if (chatDate.contains(curDate)) {
                    format = new SimpleDateFormat("hh:mm aa");
                    time = format.format(date);
                    Log.e(TAG, "Adapter Time Matches");
                } else {
                    format = new SimpleDateFormat("dd-MM-yyyy HH:mm aa");
                    time = format.format(date);
                    Log.e(TAG, "Time doesnt Match");
                }
            } catch (Exception e) {
                Log.e(TAG, "Chat Adapter Time Exception: " + e.toString());
            }

            try {
                mName.setText(map.get("NAME").toString());
            } catch (Exception e) {
                Log.e(TAG, "getView:  Name" + e.toString());
            }

            try {
                mMessage.setText(map.get("MESSAGE").toString());
                mTime.setText(time);
            } catch (Exception e) {

            }
        }

        else if(map.get("TYPE").toString().contains("IMAGE")){

            if (map.get("FROM").toString().contains(mAuth.getUid()))
                convertView = inflater.inflate(R.layout.message_image_layout_user, null);

            else
                convertView = inflater.inflate(R.layout.message_image_layout_other, null);

            ImageView imageView = convertView.findViewById(R.id.message_image_view);
            ProgressBar progressBar = convertView.findViewById(R.id.progress_circular_bar_image);
            GetImageBitmap(map.get("MESSAGE").toString(), imageView, progressBar);
        }
        return convertView;
    }

    void GetImageBitmap(final String url, final ImageView imageView, final ProgressBar progressBar){
        Log.e(TAG, "GetImageBitmap: " + url );
        final Handler handler = new Handler(Looper.getMainLooper());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FileInputStream fileInputStream;
                Bitmap bitmap = null;
                try {
                    fileInputStream = activity.openFileInput(url);
                    bitmap = BitmapFactory.decodeStream(fileInputStream);
                    final Bitmap finalBitmap = bitmap;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progressBar.setVisibility(View.GONE);
                                Drawable drawable = new BitmapDrawable(finalBitmap);
                                imageView.setImageDrawable(drawable);
                                imageView.setDrawingCacheEnabled(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    
                    Log.e(TAG, "GetImageBitmap: Bitmap");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    final StorageReference ref = storage.getReference().child("images/message/" + url);
                    ref.getBytes(1020*1024*7).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.e("TestData", "onSuccess: " + "successfully downloaded image" );
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            try {
                                Drawable drawable = new BitmapDrawable(bitmap);
                                imageView.setImageDrawable(drawable);
                                imageView.setDrawingCacheEnabled(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            progressBar.setVisibility(View.GONE);
                            saveImage(activity, bitmap, url);
                        }
                    });
                }
            }
        });

    }

    void saveImage(Context context, Bitmap bitmap, String name){
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
