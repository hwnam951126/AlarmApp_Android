package com.example.alarmapp.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtil {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatAlarmTime(LocalDateTime alarmTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");  // "07:30 AM" 형식
        return alarmTime.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatAlarmTimeWithDate(LocalDateTime alarmTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM월 dd일 (EEE)", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN);  // "오전 07:30" 형식

        // 날짜와 시간을 함께 반환
        return alarmTime.format(dateFormatter) + " " + alarmTime.format(timeFormatter);
    }
}