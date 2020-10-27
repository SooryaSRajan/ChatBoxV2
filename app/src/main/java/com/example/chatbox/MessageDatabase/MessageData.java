package com.example.chatbox.MessageDatabase;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MessageTable")
public class MessageData {

    @PrimaryKey
    @NonNull
    public String key;

    @ColumnInfo(name = "FROM")
    public String mFrom;

    @ColumnInfo (name = "TO")
    public String mTo;

    @ColumnInfo (name = "TIME")
    public String mTime;

    @ColumnInfo (name = "MESSAGE")
    public String mMessage;

    @ColumnInfo (name = "TYPE")
    public String type;

    public MessageData(@NonNull String key, String mFrom, String mTo,String mTime, String mMessage, String type){
        this.key = key;
        this.mFrom = mFrom;
        this.mTo = mTo;
        this.mTime = mTime;
        this.mMessage = mMessage;
        this.type = type;
    }




}
