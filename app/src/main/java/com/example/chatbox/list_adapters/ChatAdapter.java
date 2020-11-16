package com.example.chatbox.list_adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chatbox.ChatListActivity;
import com.example.chatbox.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatAdapter extends BaseAdapter {

    String TAG = "Chat Adapter";
    FirebaseStorage storage;
    private ArrayList<HashMap> chatList;
    StorageReference mDataRef;
    private Activity activity;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private MediaPlayer mediaPlayer = null;
    private String finalRecorderPath;
    int audioPosition;
    private boolean recordingFlag = false;
    private int recorderButtonPressedPosition = -1;
    ListView listView;

    public ChatAdapter(Activity activity, ArrayList<HashMap> chatList){
        this.chatList = chatList;
        this.activity = activity;
        this.listView = listView;

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

    static class ViewHolderPlayer {
        ImageButton playButton;
        ImageButton pauseButton;
        SeekBar seekBar;
        ProgressBar progressBar;
    }

    static class ViewHolderImage{
        ImageView messageImage;
        ProgressBar imageProgressBar;
    }

    static class ViewHolderMessage{
        TextView messageText;
        TextView messageTime;
        TextView messageName;

    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final HashMap map = chatList.get(position);
        final ViewHolderPlayer holder;
        final ViewHolderImage imageHolder;

        LayoutInflater inflater = activity.getLayoutInflater();

        /**
         * Time and Date is parsed in the following set of lines*
         * */
        String time = map.get("TIME").toString();
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

        /**
         * Message Layout
         */
        if(map.get("TYPE").toString().contains("MESSAGE")) {

            if (map.get("FROM").toString().contains(mAuth.getUid()))
                convertView = inflater.inflate(R.layout.sender, null);

            else
                convertView = inflater.inflate(R.layout.reciever, null);

            TextView mName = convertView.findViewById(R.id.user_name_list);
            TextView mMessage = convertView.findViewById(R.id.chat_text_list);
            TextView mTime = convertView.findViewById(R.id.text_time_list);


            /**
             * *Date And Time Formatter For List View Chat*
             * */

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

        /**
         * Image Layout
         */
        else if(map.get("TYPE").toString().contains("IMAGE")){

            if (map.get("FROM").toString().contains(mAuth.getUid()))
                convertView = inflater.inflate(R.layout.message_image_layout_user, null);

            else
                convertView = inflater.inflate(R.layout.message_image_layout_other, null);


            ImageView imageView = convertView.findViewById(R.id.message_image_view);
            ProgressBar progressBar = convertView.findViewById(R.id.progress_circular_bar_image);
            GetImageBitmap(map.get("MESSAGE").toString(), imageView, progressBar);
        }


        /**
         * Recorder layout*
         * */
        else if(map.get("TYPE").toString().contains("RECORDING")) {
                if (map.get("FROM").toString().contains(mAuth.getUid())) {
                    convertView = inflater.inflate(R.layout.recording_user_list, null);
                } else {
                    convertView = inflater.inflate(R.layout.recording_other_list, null);
                }

            holder = new ViewHolderPlayer();

            holder.playButton = convertView.findViewById(R.id.list_view_recording_play);
            holder.pauseButton = convertView.findViewById(R.id.list_view_recording_pause);
            holder.seekBar = convertView.findViewById(R.id.list_recording_progress_bar);
            holder.progressBar = convertView.findViewById(R.id.list_view_recording_progress_bar);
            holder.pauseButton.setVisibility(View.INVISIBLE);

                TextView mTime = convertView.findViewById(R.id.list_recording_received_time);
                mTime.setText(time);

                finalRecorderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() +
                    "/" + map.get("MESSAGE").toString().trim();
                finalRecorderPath = finalRecorderPath.trim();

            holder.progressBar.setVisibility(View.GONE);

            final File file = new File(finalRecorderPath);
                if(!file.exists()){
                    holder.playButton.setVisibility(View.INVISIBLE);
                    holder.pauseButton.setVisibility(View.INVISIBLE);
                    holder.progressBar.setVisibility(View.VISIBLE);

                    Log.e(TAG, "getView: " + "recording/" + map.get("MESSAGE").toString().trim() );
                    final StorageReference ref = storage.getReference().child("recording/" + map.get("MESSAGE").toString());
                    ref.getBytes(1020*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.e("TestData", "onSuccess: " + "successfully downloaded recording" );
                            try {
                                file.createNewFile();
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(bytes);
                                fos.close();
                                Log.e(TAG, "onSuccess: Written to local storage");

                                holder.playButton.setVisibility(View.VISIBLE);
                                holder.pauseButton.setVisibility(View.INVISIBLE);
                                holder.progressBar.setVisibility(View.INVISIBLE);


                                Log.e(TAG, "setRecording: Path " + finalRecorderPath);

                                Log.e(TAG, "onClick: Playing");

                                voiceRecordingControls(holder, position, map.get("MESSAGE").toString());

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onSuccess: " + e );
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onSuccess: " + e );
                            }

                        }
                    });
                }

                else {
                    voiceRecordingControls(holder, position, map.get("MESSAGE").toString());
                }

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

    public void stopAudioPlayer(){
        if(mediaPlayer != null){
            try{
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            catch (IllegalStateException e){

            }
        }
    }

    void voiceRecordingControls(final ViewHolderPlayer holder, final int position, final String path){
        if (position != recorderButtonPressedPosition) {
            holder.playButton.setVisibility(View.VISIBLE);
            holder.pauseButton.setVisibility(View.INVISIBLE);
        }

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                holder.playButton.setVisibility(View.INVISIBLE);
                holder.pauseButton.setVisibility(View.VISIBLE);

                finalRecorderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() +
                        "/" + path;
                finalRecorderPath = finalRecorderPath.trim();

                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.release();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onClick: " + e.toString());
                }


                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(finalRecorderPath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Media player error: " + e.toString());

                }

                if (recorderButtonPressedPosition != -1 && recorderButtonPressedPosition != position) {
                    ChatListActivity.listView.getChildAt(recorderButtonPressedPosition
                            - ChatListActivity.listView.getFirstVisiblePosition()).findViewById(R.id.list_view_recording_play).setVisibility(View.VISIBLE);
                    ChatListActivity.listView.getChildAt(recorderButtonPressedPosition
                            - ChatListActivity.listView.getFirstVisiblePosition()).findViewById(R.id.list_view_recording_pause).setVisibility(View.INVISIBLE);
                }


                recorderButtonPressedPosition = position;

                final SeekBar seekBar = ChatListActivity.listView.getChildAt(position
                        - ChatListActivity.listView.getFirstVisiblePosition()).findViewById(R.id.list_recording_progress_bar);
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (mediaPlayer != null && fromUser)
                            mediaPlayer.seekTo(progress);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                final int[] currentPosition = {0};

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                currentPosition[0] = mediaPlayer.getCurrentPosition();
                            } catch (IllegalStateException e) {
                                Log.e(TAG, "run: " + e.toString());
                                break;
                            }
                            if (mediaPlayer == null)
                                break;
                            seekBar.setProgress(currentPosition[0]);
                        }
                    }
                });


                if (recordingFlag) {
                    mediaPlayer.seekTo(audioPosition);
                    recordingFlag = false;
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                        holder.playButton.setVisibility(View.VISIBLE);
                        holder.pauseButton.setVisibility(View.INVISIBLE);
                        seekBar.setProgress(recorderButtonPressedPosition);
                        recorderButtonPressedPosition = -1;
                        seekBar.setProgress(0);

                    }
                });
            }

        });

        holder.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.playButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setVisibility(View.INVISIBLE);
                mediaPlayer.pause();
                audioPosition = mediaPlayer.getCurrentPosition();
                recordingFlag = true;
            }
        });

        Log.e(TAG, "setRecording: Path " + finalRecorderPath);

        Log.e(TAG, "onClick: Playing");
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.stop();
                    audioPosition = 0;
                    recordingFlag = false;
                    mediaPlayer.release();
                    Log.e(TAG, "onCompletion: Completed playing audio");
                }
            });

        }
    }
}


