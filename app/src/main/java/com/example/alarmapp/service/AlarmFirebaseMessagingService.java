package com.example.alarmapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.alarmapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AlarmFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // 메시지 수신 시 처리 로직
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String notificationBody = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + notificationBody);
            // 여기에서 알림을 처리하거나 사용자에게 표시

            // 알림 생성 및 표시
            showNotification(notificationBody);

            // 반복 진동 울리기
            startVibration();

        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // 토큰 갱신 시 서버에 토큰 전달
    }

    // 노티피케이션 생성 및 표시
    private void showNotification(String messageBody) {
        String channelId = "alarm_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O 이상은 Notification Channel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Alarm Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // "해제" 버튼 인텐트 생성
        Intent stopIntent = new Intent(this, NotificationActionReceiver.class);
        stopIntent.setAction("STOP_VIBRATION");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 노티피케이션 빌드
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)  // 기본 알림 아이콘
                .setContentTitle("알림 도착")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "해제", stopPendingIntent);

        notificationManager.notify(1, builder.build());
    }

    // 반복 진동 실행
    private void startVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            Log.d(TAG, "vibrator != null");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 1000, 500};  // 진동 1초, 대기 0.5초 반복
                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, 0);  // 반복(0)
                vibrator.vibrate(vibrationEffect);
            } else {
                vibrator.vibrate(new long[]{0, 1000, 500}, 0);  // API 26 미만 반복 진동
            }
        }
    }
}
