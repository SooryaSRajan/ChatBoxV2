package com.example.chatbox.user_profile_database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface daoProfile {

    @Query("SELECT * FROM user_profile ORDER BY user_key")
    List<profile> getProfile();

    @Query("DELETE FROM user_profile")
    void deleteAll();

    @Insert
    void insertProfile(profile Profile);



}
