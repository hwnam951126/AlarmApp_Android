package com.example.alarmapp.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if ("STOP_VIBRATION".equals(intent.getAction())) {
            Log.d("NotificationAction", "STOP_VIBRATION 액션 수신됨. 진동 중지.");

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
                Log.d("NotificationAction", "진동이 중지되었습니다.");
            }


            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(1);  // 알림 제거
            }

            Log.d("NotificationAction", "진동 중지 및 알림 해제됨");
        }
    }
}