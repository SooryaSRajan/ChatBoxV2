package com.example.chatbox.FCMNotification;

public class Data {

    private String user;
    private String body;
    private String title;
    private String sent;

    public Data(String user, String body, String title, String sent){
        this.body = body;
        this.sent = sent;
        this.user = user;
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getSent() {
        return sent;
    }
}
