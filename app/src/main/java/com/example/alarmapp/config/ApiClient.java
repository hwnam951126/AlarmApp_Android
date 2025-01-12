package com.example.alarmapp.config;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.alarmapp.adapter.LocalDateTimeAdapter;
import com.example.alarmapp.service.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static ApiClient instance;
    private static Retrofit retrofit = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ApiClient() {
        // HTTP 로깅 인터셉터 추가 (디버깅용)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // 커스텀 Gson 객체 생성, LocalDateTime 어댑터 추가
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // LocalDateTime 처리
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)) // Custom Gson 사용
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}
