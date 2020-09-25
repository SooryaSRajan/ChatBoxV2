package com.example.chatbox.MessageDatabase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoMessage {

    @Query("SELECT * FROM MessageTable")
    List<MessageData> getMessages();

    @Insert
    void InsertMessage(MessageData messageData);

    @Query("DELETE FROM MessageTable")
    void deleteAll();

    @Query("DELETE FROM MessageTable WHERE `key` ==:key")
    void deleteTuple(String key);

}
