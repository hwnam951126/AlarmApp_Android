package com.example.alarmapp.util;

import android.content.Context;
import android.provider.Settings;

public class DeviceUtil {
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
