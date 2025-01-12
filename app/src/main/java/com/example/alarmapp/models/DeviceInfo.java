package com.example.alarmapp.models;

public class DeviceInfo {
    private String deviceId;
    private String token;

    private DeviceInfo(Builder builder) {
        this.deviceId = builder.deviceId;
        this.token = builder.token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getToken() {
        return token;
    }

    public static class Builder {
        private String deviceId;
        private String token;

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public DeviceInfo build() {
            return new DeviceInfo(this);
        }
    }
}