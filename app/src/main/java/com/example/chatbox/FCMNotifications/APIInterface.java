package com.example.chatbox.FCMNotifications;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIInterface {

    @Headers({"Authorization: key=AAAAcbbYIW4:APA91bHefWZnNKFoFhYRjDMVDMMp41-3zjt6RWcr_tku7NmvBEDDHDmZZCpfwc51rU-VlFsCMy_5dTp3YEN_3dvoDUPp1YRteJenuYkR7z1_iosdJwzS97VZ4wYjivgUT9VCe1f9_OGv",
    "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body NotificationBody body);
}
