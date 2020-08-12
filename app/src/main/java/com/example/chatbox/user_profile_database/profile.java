package com.example.chatbox.user_profile_database;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class profile {

    @PrimaryKey
    @NonNull
    public String user_key;

    @ColumnInfo (name = "user_name")
    public String name;

    //@ColumnInfo (name = "profile_picture")
    //public Bitmap profilePic;

    public profile(@NonNull String user_key, String name){
    this.user_key = user_key;
    this.name = name;

    }
}
