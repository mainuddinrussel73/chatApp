package com.example.mainuddin.doapp;

import com.example.mainuddin.doapp.Notification.MyResponse;
import com.example.mainuddin.doapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdxP0ZfQ:APA91bHDaRTrGyBXaIZ-3EVs9xiVZwryS3saMSSRgp8U3E1bEdLWIvwVnjn2yv-sf3wcdoPmIkSZ3Uu_7FBVNGybfyYmi-1QqrsnkRDxdLXwhJMhQCI9vTQQQ51NexsszmuJC89pgn9J"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
