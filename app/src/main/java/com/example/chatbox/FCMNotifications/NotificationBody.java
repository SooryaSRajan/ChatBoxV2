package com.example.chatbox.FCMNotifications;

import com.google.gson.annotations.SerializedName;

public class NotificationBody {

    @SerializedName("to")
    private String token;

    @SerializedName("notification")
    private NotificationContent notificationContent;

    public void setToken(String token) {
        this.token = token;
    }

    public void setNotificationContent(NotificationContent notificationContent) {
        this.notificationContent = notificationContent;
    }
}

