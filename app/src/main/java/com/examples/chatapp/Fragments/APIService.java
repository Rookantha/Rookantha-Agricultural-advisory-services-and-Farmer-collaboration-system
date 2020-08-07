package com.examples.chatapp.Fragments;

import com.examples.chatapp.Notifications.MyResponse;
import com.examples.chatapp.Notifications.Sender;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

public interface APIService
{
   @Headers
           (
           {
                   "Content-Type:application/json",
                   "Authorization:key=AAAAZq6E9Ec:APA91bEH0SJKqXZuDZ8ytySAp2KTY9bZVP2zQxgH60MQRS8mp60IJJf4W238tZd1ecm5rAlT-4yU1hg7uQOHDcI2fgadqmkerOYowCz-mnyN2e7rQAQlNkQuKot-gTwiqEYpxNiKUkq5"
           }
           )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
