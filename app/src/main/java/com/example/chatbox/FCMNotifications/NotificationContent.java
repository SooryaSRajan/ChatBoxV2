package com.example.chatbox.FCMNotifications;

import com.google.gson.annotations.SerializedName;

public class NotificationContent {

     @SerializedName("title")
     String title;

     @SerializedName("body")
     String body;


    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
