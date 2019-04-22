package com.oguzhanozer.mychatapplication.fragment;

import com.oguzhanozer.mychatapplication.Notification.MyResponse;
import com.oguzhanozer.mychatapplication.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
        {
                "Content-Type:application/json",
                "Authorization:key= AAAAypacBA4:APA91bGQ8O_4ILytfJ6xRWRYhw9j9WLnlPdD8igrVBvtogxCC96tqJSOfQ1Gr5HQY4mst78T2FlGpTTzhuwukl8bhVyPuAMvJTtqkYmAy6fneldP87DuTz3zSiKgNYIhaOe0ilItr_Ms"

        }
    )




    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
