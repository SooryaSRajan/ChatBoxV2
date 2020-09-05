package com.example.chatbox.MessageDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.chatbox.user_profile_database.profile;

@Database(entities = MessageData.class, exportSchema = false, version = 1)
public abstract class MessageDatabase extends RoomDatabase {

    private static final String DB_NAME = "MESSAGE_DATABASE";
    private static MessageDatabase instance;

    public static synchronized MessageDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MessageDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
    public abstract DaoMessage dao();
}
