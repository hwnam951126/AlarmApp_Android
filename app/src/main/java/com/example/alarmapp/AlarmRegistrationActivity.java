package com.example.alarmapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alarmapp.config.ApiClient;
import com.example.alarmapp.models.Alarm;
import com.example.alarmapp.models.NotificationRequest;
import com.example.alarmapp.service.ApiService;
import com.example.alarmapp.util.DeviceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "AlarmRegistration";
    private TimePicker timePicker;
    private Button submitButton, cancelButton;
    private EditText alarmNameEditText;
    private ToggleButton monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    private boolean isEditMode = false;
    private Long alarmId = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_registration);

        initViews();
        setupListeners();

        if (getIntent().hasExtra("alarm_id")) {
            if (getIntent().hasExtra("alarm_id")) {
                isEditMode = true;
                alarmId = getIntent().getLongExtra("alarm_id", -1);
                String alarmName = getIntent().getStringExtra("alarm_name");
                List<String> alarmDays = getIntent().getStringArrayListExtra("alarm_days");
                String scheduledTime = getIntent().getStringExtra("scheduled_time");

                // 이전 데이터로 UI 설정
                alarmNameEditText.setText(alarmName);
                setSelectedDays(alarmDays);

                if (scheduledTime != null) {
                    LocalDateTime time = LocalDateTime.parse(scheduledTime);
                    setTimePickerWith12Hour(time);
                }
            }
        }
    }

    private void setTimePickerWith12Hour(LocalDateTime time) {
        int hour = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hour = time.getHour();
        }
        int minute = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            minute = time.getMinute();
        }

        boolean isPM = hour >= 12;
        int displayHour = hour % 12;
        displayHour = displayHour == 0 ? 12 : displayHour; // 0시는 12로 변경

        timePicker.setIs24HourView(false); // 12시간제 모드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(displayHour);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(minute);
        }

        // AM/PM 버튼도 설정
        ToggleButton amPmToggle = findViewById(R.id.amPmToggle);
        amPmToggle.setChecked(isPM);
    }

    private void initViews() {
        timePicker = findViewById(R.id.timePicker);
        alarmNameEditText = findViewById(R.id.alarmName);
        submitButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        monday = findViewById(R.id.toggle_monday);
        tuesday = findViewById(R.id.toggle_tuesday);
        wednesday = findViewById(R.id.toggle_wednesday);
        thursday = findViewById(R.id.toggle_thursday);
        friday = findViewById(R.id.toggle_friday);
        saturday = findViewById(R.id.toggle_saturday);
        sunday = findViewById(R.id.toggle_sunday);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupListeners() {
        String deviceId = DeviceUtil.getDeviceId(this);
        Log.d(TAG, "Device ID: " + deviceId);

        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                if (isEditMode) {
                    updateAlarm(deviceId); // 수정 모드에서 알람 업데이트
                } else {
                    createAlarm(deviceId); // 등록 모드에서 새 알람 생성
                }
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private boolean validateInput() {
        if (getSelectedDays().isEmpty()) {
            Toast.makeText(this, "적어도 하나의 요일을 선택하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createAlarm(String deviceId) {
        LocalDateTime scheduledTime = getSelectedTime();
        Long id = System.currentTimeMillis();

        NotificationRequest request = NotificationRequest.builder()
                .id(id)
                .deviceId(deviceId)
                .scheduledTime(String.valueOf(scheduledTime))
                .daysOfWeek(getSelectedDays())
                .alarmName(alarmNameEditText.getText().toString())
                .build();

        ApiService apiService = ApiClient.getInstance().getApiService();
        Call<Void> call = apiService.createAlarm(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AlarmRegistrationActivity.this, "알림 시간이 서버에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AlarmRegistrationActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "서버 요청 실패: " + t.getMessage(), t);
                Toast.makeText(AlarmRegistrationActivity.this, "서버 요청 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateAlarm(String deviceId) {
        LocalDateTime scheduledTime = getSelectedTime();


        NotificationRequest request = NotificationRequest.builder()
                .id(alarmId)
                .deviceId(deviceId)
                .scheduledTime(String.valueOf(scheduledTime))
                .daysOfWeek(getSelectedDays())
                //.alarmName(alarmNameEditText.getText().toString())
                .build();

        ApiService apiService = ApiClient.getInstance().getApiService();
        Call<Void> call = apiService.updateAlarm(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AlarmRegistrationActivity.this, "알림이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AlarmRegistrationActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "서버 요청 실패: " + t.getMessage(), t);
                Toast.makeText(AlarmRegistrationActivity.this, "서버 요청 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadAlarmData(Long alarmId) {
        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getAlarmById(alarmId).enqueue(new Callback<Alarm>() {
            @Override
            public void onResponse(Call<Alarm> call, Response<Alarm> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Alarm alarm = response.body();
                    // 시간, 요일, 알람 이름을 UI에 설정
                    timePicker.setHour(alarm.getScheduledTime().getHour());
                    timePicker.setMinute(alarm.getScheduledTime().getMinute());
                    alarmNameEditText.setText(alarm.getAlarmName());
                    // 요일 설정
                    setSelectedDays(alarm.getDaysOfWeek());
                }
            }

            @Override
            public void onFailure(Call<Alarm> call, Throwable t) {
                Log.e(TAG, "알람 데이터 로드 실패", t);
            }
        });
    }

    private void setSelectedDays(List<String> daysOfWeek) {
        monday.setChecked(daysOfWeek.contains("Monday"));
        tuesday.setChecked(daysOfWeek.contains("Tuesday"));
        wednesday.setChecked(daysOfWeek.contains("Wednesday"));
        thursday.setChecked(daysOfWeek.contains("Thursday"));
        friday.setChecked(daysOfWeek.contains("Friday"));
        saturday.setChecked(daysOfWeek.contains("Saturday"));
        sunday.setChecked(daysOfWeek.contains("Sunday"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocalDateTime getSelectedTime() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        return LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0);
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        if (monday.isChecked()) selectedDays.add("Monday");
        if (tuesday.isChecked()) selectedDays.add("Tuesday");
        if (wednesday.isChecked()) selectedDays.add("Wednesday");
        if (thursday.isChecked()) selectedDays.add("Thursday");
        if (friday.isChecked()) selectedDays.add("Friday");
        if (saturday.isChecked()) selectedDays.add("Saturday");
        if (sunday.isChecked()) selectedDays.add("Sunday");
        return selectedDays;
    }
}