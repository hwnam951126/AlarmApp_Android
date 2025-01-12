package com.example.alarmapp.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
    private Long id;

    private String deviceId;

    private String alarmName;

    private List<String> daysOfWeek;  // 요일 필드 추가

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String scheduledTime; // String 타입으로 유지
}