package com.example.alarmapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmapp.adapter.AlarmListAdapter;
import com.example.alarmapp.config.ApiClient;
import com.example.alarmapp.models.Alarm;
import com.example.alarmapp.models.DeviceInfo;
import com.example.alarmapp.service.ApiService;
import com.example.alarmapp.util.DeviceUtil;
import com.example.alarmapp.util.TimeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements AlarmListAdapter.AlarmActionCallback {

    private static final String TAG = "MainActivity";
    private RecyclerView alarmListRecyclerView;
    private AlarmListAdapter alarmListAdapter;
    private List<Alarm> alarmList;
    private ActionMode actionMode;

    // 가장 가까운 알람을 표시할 텍스트뷰
    private TextView closestAlarmTimeTextView;
    private TextView closestAlarmRemainingTimeTextView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        String deviceId = DeviceUtil.getDeviceId(this);
        Log.d(TAG, "Device ID: " + deviceId);

        //단말 정보 전송
        sendDeviceInfoToServer(deviceId);

        //알람 목록 불러오기
        fetchAlarms(deviceId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        String deviceId = DeviceUtil.getDeviceId(this);
        fetchAlarms(deviceId); // 메인 페이지로 돌아올 때 알람 목록을 갱신
    }

    // UI 초기화 및 리스너 설정
    private void setupUI() {
        closestAlarmTimeTextView = findViewById(R.id.closestAlarmTimeTextView);
        closestAlarmRemainingTimeTextView = findViewById(R.id.closestAlarmRemainingTimeTextView);

        // 알람 리스트 RecyclerView 초기화
        alarmListRecyclerView = findViewById(R.id.alarmListRecyclerView);
        alarmListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 플로팅 액션 버튼 클릭 시 알람 등록 화면으로 이동
        FloatingActionButton addAlarmButton = findViewById(R.id.addAlarmButton);
        addAlarmButton.setOnClickListener(v -> {
            Log.d(TAG, "+ 버튼 클릭됨");
            startActivity(new Intent(MainActivity.this, AlarmRegistrationActivity.class));
        });
    }



    // 서버에서 알람 리스트를 가져와서 RecyclerView에 표시하고 가장 가까운 알람 계산
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchAlarms(String deviceId) {
        ApiService apiService = ApiClient.getInstance().getApiService();

        apiService.getAlarms(deviceId).enqueue(new Callback<List<Alarm>>() {
            @Override
            public void onResponse(@NonNull Call<List<Alarm>> call, @NonNull Response<List<Alarm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    alarmList = response.body();
                    Log.d(TAG, "알람 리스트 로드 성공");

                    // 알람 리스트 UI 업데이트
                    updateUIWithAlarms();

                    // 가장 가까운 알람 UI 업데이트
                    updateClosestAlarm();
                } else {
                    Toast.makeText(MainActivity.this, "알람 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Alarm>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching alarms", t);
            }
        });
    }

    // 알람 리스트 업데이트
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateUIWithAlarms() {
        alarmListAdapter = new AlarmListAdapter(MainActivity.this, alarmList, this);
        alarmListRecyclerView.setAdapter(alarmListAdapter);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendDeviceInfoToServer(String deviceId) {
        ApiService apiService = ApiClient.getInstance().getApiService();

        // ✅ FCM 토큰 발급 및 전송
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "FCM 토큰 가져오기 실패", task.getException());
                        return;
                    }

                    // ✅ FCM 토큰 가져오기
                    String token = task.getResult();
                    Log.d(TAG, "FCM 토큰: " + token);

                    // ✅ Builder 패턴으로 DeviceInfo 객체 생성
                    DeviceInfo deviceInfo = new DeviceInfo.Builder()
                            .setDeviceId(deviceId)
                            .setToken(token)  // 🔥 FCM 토큰으로 변경
                            .build();

                    // ✅ 서버로 단말 정보 전송
                    Call<Void> call = apiService.sendDeviceInfo(deviceInfo);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "단말 정보 전송 성공");
                            } else {
                                Log.e(TAG, "단말 정보 전송 실패: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "단말 정보 전송 오류", t);
                        }
                    });
                });
    }

    @Override
    public void startActionMode(int position) {
        if (actionMode != null) {
            return;
        }

        actionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_delete, menu);  // 삭제 메뉴 추가
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    // 삭제 버튼 클릭 시 처리
                    alarmListAdapter.deleteAlarm(position);
                    mode.finish();  // ActionMode 종료
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
            }
        });
    }

    // 가장 가까운 알람을 찾아서 상단 UI 업데이트
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateClosestAlarm() {
        if (alarmList != null && !alarmList.isEmpty()) {
            Alarm closestAlarm = Collections.min(alarmList, Comparator.comparing(Alarm::getScheduledTime));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime closestTime = closestAlarm.getScheduledTime();
            Duration timeUntilAlarm = Duration.between(now, closestTime);

            closestAlarmTimeTextView.setText(TimeUtil.formatAlarmTimeWithDate(closestTime));
            if (!timeUntilAlarm.isNegative()) {
                closestAlarmRemainingTimeTextView.setText(formatTimeRemainingMessage(timeUntilAlarm, closestTime));
            } else {
                closestAlarmRemainingTimeTextView.setText("알람 시간이 지났습니다.");
            }
        } else {
            closestAlarmTimeTextView.setText("설정된 알람이 없습니다.");
            closestAlarmRemainingTimeTextView.setText("");
        }
    }

    // 시간 남은 알림 메시지 포맷팅
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatTimeRemainingMessage(Duration timeUntilAlarm, LocalDateTime alarmTime) {
        long totalMinutes = timeUntilAlarm.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String formattedAlarmTime = TimeUtil.formatAlarmTimeWithDate(alarmTime);

        if (hours > 0) {
            return hours + "시간 " + minutes + "분 후에 알림이 울립니다.\n" + formattedAlarmTime;
        } else {
            return minutes + "분 후에 알림이 울립니다.\n" + formattedAlarmTime;
        }
    }
}