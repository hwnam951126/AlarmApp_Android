package com.example.alarmapp.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class Alarm {
    private Long id;
    private String deviceId;
    private String remainingTimeMessage;
    private String description;
    private String alarmName;
    private List<String> daysOfWeek;
    private LocalDateTime scheduledTime;
    private LocalDateTime alarmTime;
    private boolean enabled;

}