package com.example.alarmapp.service;


import com.example.alarmapp.models.Alarm;
import com.example.alarmapp.models.DeviceInfo;
import com.example.alarmapp.models.NotificationRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("alarms")
    Call<Void> createAlarm(@Body NotificationRequest request);

    @POST("device/add")
    Call<Void> sendDeviceInfo(@Body DeviceInfo deviceInfo);

    @GET("alarms")
    Call<List<Alarm>> getAlarms(@Query("deviceId") String deviceId);

    @GET("alarms/{id}")
    Call<Alarm> getAlarmById(@Path("id") Long alarmId);

    @POST("alarms/update")
    Call<Void> updateAlarm(@Body NotificationRequest request);

    @POST("/alarms/{id}/status")
    Call<Void> updateAlarmStatus(@Path("id") Long alarmId, @Body boolean isEnabled);

    @POST("alarms/delete")
    Call<Void> deleteAlarm(@Query("id") Long id);

    @POST("alarms/enable")
    Call<Void> enableAlarm(@Query("id") Long alarmId);

    @POST("alarms/disable")
    Call<Void> disableAlarm(@Query("id") Long alarmId);
}
