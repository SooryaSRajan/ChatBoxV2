package com.example.chatbox.user_profile_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = profile.class, exportSchema = false, version = 1)
public abstract class UserProfileTable extends RoomDatabase {


    private static final String DB_NAME = "texts_DB";
    private static UserProfileTable instance;

    public static synchronized UserProfileTable getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), UserProfileTable.class, DB_NAME)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
    public abstract daoProfile dao();
}
