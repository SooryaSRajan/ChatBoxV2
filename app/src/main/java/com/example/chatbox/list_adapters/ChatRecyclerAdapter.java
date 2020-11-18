package com.example.chatbox.list_adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbox.ChatListActivity;
import com.example.chatbox.MessageDatabase.MessageDatabase;
import com.example.chatbox.R;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final int TYPE_MESSAGE_USER = 0;
    private final int TYPE_MESSAGE_OTHER = 1;
    private final int TYPE_IMAGE_USER = 2;
    private final int TYPE_IMAGE_OTHER = 3;
    private final int TYPE_RECORDING_USER = 4;
    private final int TYPE_RECORDING_OTHER = 5;
    private final int TYPE_INVALID = -1;
    private FirebaseStorage storage;
    private StorageReference mDataRef;
    public int position = -1;
    public int snackPosition = -1;
    public int snackBarSelectedListPosition = -1;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    private DatabaseReference refMain = database.getReference().child("UNREAD MESSAGE");
    private String userId;
    private ValueEventListener ConcurrentUserNodeListener;
    private String TAG = "ChatRecyclerAdapter";

    private ArrayList<HashMap> mapArrayList = new ArrayList<>();
    private ArrayList<String> tokenList = new ArrayList<>();
    private View rootView;
    private Activity activity;
    private Snackbar snackBarUserMessage, snackBarOtherMessage, snackBarUserExtra, snackBarOtherExtra;

    public ChatRecyclerAdapter(ArrayList<HashMap> mapArrayList, View rootView, Activity activity) {

        this.mapArrayList = mapArrayList;
        this.rootView = rootView;
        this.activity = activity;
        storage = FirebaseStorage.getInstance();
        mDataRef = storage.getReferenceFromUrl("gs://chat-box-v2.appspot.com");
        SnackBarUserMessageView();
        SnackBarOtherMessageView();
        SnackBarUserExtraView();
        SnackBarOtherExtraView();

        ConcurrentUserNodeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap!=null) {
                        Log.e(TAG, "onDataChange: Token " + snap.getKey());
                        if(!Objects.requireNonNull(snap.getKey()).contains(FirebaseInstanceId.getInstance().getToken())){
                            tokenList.add(snap.getKey());
                            Log.e(TAG, "onDataChange: Added token: " + snap.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.child("CONCURRENT USERS").child(mAuth.getUid()).addValueEventListener(ConcurrentUserNodeListener);

    }

    /**
     * View Holder class for User Message view
     */
    class UserMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView messageTime;
        public TextView messageName;

        public UserMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            messageName = itemView.findViewById(R.id.user_name_list);
            messageText = itemView.findViewById(R.id.chat_text_list);
            messageTime = itemView.findViewById(R.id.text_time_list);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarUserMessage.show();
                    }

                    else if(snackPosition == (int) itemView.getTag()){
                        CopyMessage(snackBarSelectedListPosition);
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        Toast.makeText(activity, "Message Copied", Toast.LENGTH_SHORT).show();
                        AddDashBoard();
                        snackBarUserMessage.dismiss();
                        snackPosition = -1;
                        snackBarSelectedListPosition = -1;
                    }

                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarUserMessage.dismiss();
                        snackBarSelectedListPosition = -1;
                        snackPosition = -1;
                    }
                }
            });

        }
    }

    /**
     * View Holder class for Other Message View
     */
    class OtherMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView messageTime;
        public TextView messageName;

        public OtherMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            messageName = itemView.findViewById(R.id.user_name_list);
            messageText = itemView.findViewById(R.id.chat_text_list);
            messageTime = itemView.findViewById(R.id.text_time_list);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarOtherMessage.show();
                    }

                    else if(snackPosition == (int) itemView.getTag()){
                        CopyMessage(snackBarSelectedListPosition);
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        Toast.makeText(activity, "Message Copied", Toast.LENGTH_SHORT).show();
                        AddDashBoard();
                        snackBarOtherMessage.dismiss();
                        snackPosition = -1;
                        snackBarSelectedListPosition = -1;
                    }

                    return true;
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        snackBarSelectedListPosition = -1;
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarOtherMessage.dismiss();
                        snackPosition = -1;
                    }
                }
            });

        }
    }

    /**
     * View Holder class for User Image View
     */
    class UserImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public ProgressBar progressBar;

        public UserImageViewHolder(@NonNull final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.message_image_view);
            progressBar = itemView.findViewById(R.id.progress_circular_bar_image);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarUserExtra.show();
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarUserExtra.dismiss();
                        snackBarSelectedListPosition = -1;
                        snackPosition = -1;
                    }

                    else{
                        Toast.makeText(activity, "Clicked", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    /**
     * View Holder class for Other Image View
     */
    class OtherImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public ProgressBar progressBar;

        public OtherImageViewHolder(@NonNull final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.message_image_view);
            progressBar = itemView.findViewById(R.id.progress_circular_bar_image);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarOtherExtra.show();
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarOtherExtra.dismiss();
                        snackBarSelectedListPosition = -1;
                        snackPosition = -1;
                    }

                    else{
                        Toast.makeText(activity, "Clicked", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    /**
     * View Holder class for User Recorder View
     */
    public class UserRecordingViewHolder extends RecyclerView.ViewHolder{

        public ImageButton playButton;
        public ImageButton pauseButton;
        public SeekBar seekBar;
        public ProgressBar progressBar;

        public UserRecordingViewHolder(@NonNull final View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.list_view_recording_play);
            pauseButton = itemView.findViewById(R.id.list_view_recording_pause);
            seekBar = itemView.findViewById(R.id.list_recording_progress_bar);
            progressBar = itemView.findViewById(R.id.list_view_recording_progress_bar);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);

                    if(position != -1 && position != getAdapterPosition())
                        notifyItemChanged(position);

                    position = getAdapterPosition();

                }
            });

            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarUserExtra.show();
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarUserExtra.dismiss();
                        snackBarSelectedListPosition = -1;
                        snackPosition = -1;
                    }

                }
            });

        }
    }

    /**
     * View Holder class for Other Recorder View
     */
    class OtherRecordingViewHolder extends RecyclerView.ViewHolder{

        public ImageButton playButton;
        public ImageButton pauseButton;
        public SeekBar seekBar;
        public ProgressBar progressBar;

        public OtherRecordingViewHolder(@NonNull final View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.list_view_recording_play);
            pauseButton = itemView.findViewById(R.id.list_view_recording_pause);
            seekBar = itemView.findViewById(R.id.list_recording_progress_bar);
            progressBar = itemView.findViewById(R.id.list_view_recording_progress_bar);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);

                    if(position != -1 && position != getAdapterPosition())
                        notifyItemChanged(position);

                    position = getAdapterPosition();

                }
            });

            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(snackPosition == -1){
                        snackPosition = (int) itemView.getTag();
                        snackBarSelectedListPosition = getAdapterPosition();
                        itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
                        RemoveDashBoard();
                        snackBarOtherExtra.show();
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(snackPosition == (int) itemView.getTag()){
                        itemView.setBackgroundColor(Color.argb(0,0,0,0));
                        AddDashBoard();
                        snackBarOtherExtra.dismiss();
                        snackBarSelectedListPosition = -1;
                        snackPosition = -1;
                    }
                }
            });


        }
    }

    /**
     * Creates and initializes view holder for respective type
     * @param parent Returns the parent view group
     * @param viewType Returns the type of layout as defined.
     * @return Returns the respective view Holder to the recycler view to inflate
     */

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder " );
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == TYPE_MESSAGE_USER){
            viewHolder = new UserMessageViewHolder(inflater.inflate(R.layout.sender, parent, false));
        }

        else if(viewType == TYPE_MESSAGE_OTHER){
            viewHolder = new OtherMessageViewHolder(inflater.inflate(R.layout.reciever, parent, false));
        }

        else if(viewType == TYPE_IMAGE_USER){
            viewHolder = new UserImageViewHolder(inflater.inflate(R.layout.message_image_layout_user, parent, false));
        }

        else if(viewType == TYPE_IMAGE_OTHER){
            viewHolder = new OtherImageViewHolder(inflater.inflate(R.layout.message_image_layout_other, parent, false));
        }

        else if(viewType == TYPE_RECORDING_USER){
            viewHolder = new UserRecordingViewHolder(inflater.inflate(R.layout.recording_user_list, parent, false));
        }

        else if(viewType == TYPE_RECORDING_OTHER){
            viewHolder = new OtherRecordingViewHolder(inflater.inflate(R.layout.recording_other_list, parent, false));
        }

        assert viewHolder != null;
        return viewHolder;
    }

    /**
     * Binds view holder and the view holder elements with values
     * @param holder Returns current view in the recycler view
     * @param position Returns current view position
     */

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder " );
        holder.itemView.setTag(position);

        if((int)holder.itemView.getTag() != snackPosition){
            holder.itemView.setBackgroundColor(Color.argb(0,0,0,0));
        }
        else{
            holder.itemView.setBackgroundColor(Color.argb(42, 0, 250, 230));
        }

        if(getItemViewType(position) == TYPE_MESSAGE_USER){
            try {
                ((UserMessageViewHolder)holder).messageName.setText(mapArrayList.get(position).get("NAME").toString());
            } catch (Exception e) {}
            try {
                ((UserMessageViewHolder)holder).messageText.setText(mapArrayList.get(position).get("MESSAGE").toString());
            } catch (Exception e) {}
            try {
                ((UserMessageViewHolder)holder).messageTime.setText(GetTime(position));
            } catch (Exception e) {}
        }

        else if(getItemViewType(position) == TYPE_MESSAGE_OTHER){
            try {
                ((OtherMessageViewHolder)holder).messageName.setText(mapArrayList.get(position).get("NAME").toString());
            } catch (Exception e) {}
            try {
                ((OtherMessageViewHolder)holder).messageText.setText(mapArrayList.get(position).get("MESSAGE").toString());
            } catch (Exception e) {}
            try {
                ((OtherMessageViewHolder)holder).messageTime.setText(GetTime(position));
            } catch (Exception e) {}
        }

        else if(getItemViewType(position) == TYPE_IMAGE_USER){
            GetImageBitmap(mapArrayList.get(position).get("MESSAGE").toString(), ((UserImageViewHolder)holder).imageView, ((UserImageViewHolder)holder).progressBar);
        }

        else if(getItemViewType(position) == TYPE_IMAGE_OTHER){
            GetImageBitmap(mapArrayList.get(position).get("MESSAGE").toString(), ((OtherImageViewHolder)holder).imageView, ((OtherImageViewHolder)holder).progressBar);
        }

        else if(getItemViewType(position) == TYPE_RECORDING_USER){
            ((UserRecordingViewHolder)holder).playButton.setVisibility(View.VISIBLE);
            ((UserRecordingViewHolder)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((UserRecordingViewHolder)holder).progressBar.setVisibility(View.INVISIBLE);
            ((UserRecordingViewHolder)holder).seekBar.setProgress(0);
        }

        else if(getItemViewType(position) == TYPE_RECORDING_OTHER){
            ((OtherRecordingViewHolder)holder).playButton.setVisibility(View.VISIBLE);
            ((OtherRecordingViewHolder)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((OtherRecordingViewHolder)holder).progressBar.setVisibility(View.INVISIBLE);
            ((OtherRecordingViewHolder)holder).seekBar.setProgress(0);

        }
    }

    /**
     * Returns the size of the array list used
     * @return Returns size of list
     */

    @Override
    public int getItemCount() {
        return mapArrayList.size();
    }

    /**
     * Creates snack bar for user message
     */

    private void SnackBarUserMessageView(){
        snackBarUserMessage = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackBarUserMessage.getView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View snackView = inflater.inflate(R.layout.snack_bar_message_user, null);
        Button unSend = snackView.findViewById(R.id.unsend);
        Button remove = snackView.findViewById(R.id.remove);

        unSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnSendMessage(snackBarSelectedListPosition);
                snackBarSelectedListPosition = -1;
                snackPosition = -1;
                AddDashBoard();
                snackBarUserMessage.dismiss();
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveLocalMessage(snackBarSelectedListPosition);
                snackBarSelectedListPosition = -1;
                snackPosition = -1;
                AddDashBoard();
                snackBarUserMessage.dismiss();
            }
        });

        layout.setPadding(0,0,0,0);
        layout.addView(snackView, 0);


    }

    /**
     * Creates snack bar for other message
     */

    private void SnackBarOtherMessageView(){
        snackBarOtherMessage = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackBarOtherMessage.getView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View snackView = inflater.inflate(R.layout.snack_bar_message_other, null);
        layout.setPadding(0,0,0,0);
        layout.addView(snackView, 0);

        Button remove = snackView.findViewById(R.id.remove);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveLocalMessage(snackBarSelectedListPosition);
                snackBarSelectedListPosition = -1;
                snackPosition = -1;
                AddDashBoard();
                snackBarOtherMessage.dismiss();
            }
        });

    }

    /**
     * Creates snack bar for user recording/video/image
     */

    private void SnackBarUserExtraView(){
        snackBarUserExtra = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackBarUserExtra.getView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View snackView = inflater.inflate(R.layout.snack_bar_extra_user, null);
        layout.setPadding(0,0,0,0);
        layout.addView(snackView, 0);
    }

    /**
     * Creates snack bar for other recording/video/image
     */

    private void SnackBarOtherExtraView(){
        snackBarOtherExtra = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackBarOtherExtra.getView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View snackView = inflater.inflate(R.layout.snack_bar_extra_other, null);
        layout.setPadding(0,0,0,0);
        layout.addView(snackView, 0);
    }

    /**
     * Returns the type of view being inflated into the recycler view
     * @param position Current position of the view
     * @return Returns an integer constant to determine type of layout to be inflated by the view holders
     */

    @Override
    public int getItemViewType(int position) {
        if (mapArrayList.get(position).get("TYPE").toString().contains("MESSAGE")) {
            if (mapArrayList.get(position).get("FROM").toString().contains(mAuth.getUid())) {
                return TYPE_MESSAGE_USER;
            }
            else{
                return TYPE_MESSAGE_OTHER;
            }
        }

        else if (mapArrayList.get(position).get("TYPE").toString().contains("IMAGE")) {
            if (mapArrayList.get(position).get("FROM").toString().contains(mAuth.getUid())) {
                return TYPE_IMAGE_USER;
            }
            else{
                return TYPE_IMAGE_OTHER;
            }
        }

        else if (mapArrayList.get(position).get("TYPE").toString().contains("RECORDING")) {
            if (mapArrayList.get(position).get("FROM").toString().contains(mAuth.getUid())) {
                return TYPE_RECORDING_USER;
            }
            else{
                return TYPE_RECORDING_OTHER;
            }
        }

        else{
            return TYPE_INVALID;
        }
    }

    /**
     * Loads Bitmap Image from local storage/cloud storage
     * @param url URL of image
     * @param imageView Current image view to be inflated with bitmap
     * @param progressBar Progress bar of loading image
     */

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

    /**
     * Saves image to local storage
     * @param context Current context of app
     * @param bitmap Bitmap data of the image
     * @param name External storage path/ file name
     */

    void saveImage(Context context, Bitmap bitmap, String name){
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns time of the message received/send as formatted time
     * @param position Current position of the view/ list
     * @return Returns time as string
     */

    private String GetTime(int position) {
        String time = mapArrayList.get(position).get("TIME").toString();
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
        return time;
    }

    public void RemoveDashBoard(){
        activity.findViewById(R.id.recorder_button).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.message_box).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.add_resource).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.message_button).setVisibility(View.INVISIBLE);
    }

    public void AddDashBoard(){
        if(!(activity.findViewById(R.id.recorder_dashboard).getVisibility() == View.VISIBLE)){
            activity.findViewById(R.id.message_box).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.message_button).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.add_resource).setVisibility(View.VISIBLE);
        }
        else{
            activity.findViewById(R.id.recorder_dashboard).setVisibility(View.VISIBLE);
        }
        activity.findViewById(R.id.recorder_button).setVisibility(View.VISIBLE);

    }

    private void UnSendMessage(final int position) {
        userId = mapArrayList.get(position).get("TO").toString();
        final String keys = mapArrayList.get(position).get("KEY").toString();

        databaseReference.child("NEW MESSAGE").child(keys).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference refer) {

                databaseReference.child("CONCURRENT MESSAGE").child(mAuth.getUid()).child(userId).child(keys).removeValue();

                refMain.child(keys).removeValue();

                mapArrayList.remove(position);
                notifyItemRemoved(position);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MessageDatabase database = MessageDatabase.getInstance(activity);
                            database.dao().deleteTuple(keys);
                        } catch (Exception e) {

                        }
                    }
                });

                databaseReference.child("UNSENT MESSAGE KEY").child(userId).child(mAuth.getUid()).child(keys).setValue("ADDED");
                for (String tokens : tokenList)
                    databaseReference.child("CONCURRENT TOKENS").child(mAuth.getUid()).child(tokens).child(userId).child(keys).removeValue();


                try {
                    if (mapArrayList.size() == position) {
                        if (mapArrayList.get(position - 1).get("TYPE").toString().contains("MESSAGE")) {
                            databaseReference.child("LAST MESSAGE").child(mAuth.getUid()).child(userId).setValue(mapArrayList.get(position - 1).get("MESSAGE").toString());
                            databaseReference.child("LAST MESSAGE").child(userId).child(mAuth.getUid()).setValue(mapArrayList.get(position - 1).get("MESSAGE").toString());
                        }
                        else if(mapArrayList.get(position - 1).get("TYPE").toString().contains("IMAGE")){
                            databaseReference.child("LAST MESSAGE").child(mAuth.getUid()).child(userId).setValue("you sent an image");
                            databaseReference.child("LAST MESSAGE").child(userId).child(mAuth.getUid()).setValue("sent you an image");
                        }

                        else if(mapArrayList.get(position - 1).get("TYPE").toString().contains("RECORDING")){
                            databaseReference.child("LAST MESSAGE").child(mAuth.getUid()).child(userId).setValue("you sent a recording");
                            databaseReference.child("LAST MESSAGE").child(userId).child(mAuth.getUid()).setValue("sent you a recording");
                        }
                    }

                } catch (Exception e) {

                    if (mapArrayList.size() == 0) {
                        databaseReference.child("LAST MESSAGE").child(mAuth.getUid()).child(userId).setValue("");
                        databaseReference.child("LAST MESSAGE").child(userId).child(mAuth.getUid()).setValue("");
                    }
                }

            }
        });
    }

    private void RemoveLocalMessage(int position){
        userId = mapArrayList.get(position).get("TO").toString();
        final String keys = mapArrayList.get(position).get("KEY").toString();

        mapArrayList.remove(position);
        notifyItemRemoved(position);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageDatabase database = MessageDatabase.getInstance(activity);
                    database.dao().deleteTuple(keys);
                } catch (Exception e) {

                }
            }
        });


    }

    /**
     * Copies message to clip board
     * @param position Current position of view/list
     */

    private void CopyMessage(int position){
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Chat Box Message", mapArrayList.get(position).get("MESSAGE").toString());
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
    }
}
