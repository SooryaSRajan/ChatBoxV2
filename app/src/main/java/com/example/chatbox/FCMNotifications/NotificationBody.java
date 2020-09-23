package com.example.chatbox.FCMNotifications;

import com.google.gson.annotations.SerializedName;

public class NotificationBody {

    @SerializedName("to")
    private String token;


    @SerializedName("notification")
    private NotificationContent data;

    @SerializedName("priority")
    private String priority;

    public void setToken(String token) {
        this.token = token;
    }

    public void setNotificationContent(NotificationContent notificationContent) {
        this.priority = "high";
        this.data = notificationContent;
    }
}

