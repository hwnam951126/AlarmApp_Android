package com.example.alarmapp.adapter;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmapp.R;
import com.example.alarmapp.config.ApiClient;
import com.example.alarmapp.models.Alarm;
import com.example.alarmapp.service.ApiService;

import java.util.ArrayList;
import java.util.List;
import com.example.alarmapp.AlarmRegistrationActivity;

import retrofit2.Call;


public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;
    private Context context;
    private final AlarmActionCallback actionCallback;

    public interface AlarmActionCallback {
        void startActionMode(int position);
    }

    public AlarmListAdapter(Context context, List<Alarm> alarmList, AlarmActionCallback actionCallback) {
        this.context = context;
        this.alarmList = alarmList;
        this.actionCallback = actionCallback;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);

        // 알람 시간 설정
        holder.alarmTimeTextView.setText(alarm.getScheduledTime().toLocalTime().toString());

        // 반복 요일 설정
        holder.repeatDaysTextView.setText(String.join(", ", alarm.getDaysOfWeek()));

        holder.alarmSwitch.setOnCheckedChangeListener(null);

        holder.alarmSwitch.setChecked(alarm.isEnabled());

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 상태가 변했을 때만 서버에 요청
            if (alarm.isEnabled() != isChecked) {
                ApiService apiService = ApiClient.getInstance().getApiService();

                // ✅ 상태에 따라 다른 API 호출
                Call<Void> call = isChecked
                        ? apiService.enableAlarm(alarm.getId())   // 활성화 API 호출
                        : apiService.disableAlarm(alarm.getId()); // 비활성화 API 호출

                call.enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            // 서버 상태 업데이트 성공
                            alarm.setEnabled(isChecked);  // 로컬 상태 업데이트
                            Log.d("AlarmListAdapter", "알람 상태 업데이트 성공");
                        } else {
                            // 실패 처리, 스위치 상태를 원래대로 되돌림
                            holder.alarmSwitch.setChecked(!isChecked);
                            Log.e("AlarmListAdapter", "알람 상태 업데이트 실패: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        // 네트워크 에러 등의 실패 처리
                        holder.alarmSwitch.setChecked(!isChecked);
                        Log.e("AlarmListAdapter", "알람 상태 업데이트 네트워크 오류", t);
                    }
                });
            }
        });

        // 알람 아이템 클릭 시 수정 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AlarmRegistrationActivity.class);
            intent.putExtra("alarm_id", alarm.getId());
            intent.putExtra("alarm_name", alarm.getAlarmName());
            intent.putExtra("alarm_days", new ArrayList<>(alarm.getDaysOfWeek()));
            intent.putExtra("scheduled_time", alarm.getScheduledTime().toString());
            context.startActivity(intent);
        });

        // 길게 눌러서 ActionMode 호출
        holder.itemView.setOnLongClickListener(v -> {
            actionCallback.startActionMode(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    // 알람 삭제 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteAlarm(int position) {
        Alarm alarm = alarmList.get(position);
        ApiService apiService = ApiClient.getInstance().getApiService();

        apiService.deleteAlarm(alarm.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 삭제된 경우 리스트에서 제거하고 UI 업데이트
                    alarmList.remove(position);
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                // 에러 처리
                t.printStackTrace();
            }
        });
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {

        TextView alarmTimeTextView;
        TextView repeatDaysTextView;
        Switch alarmSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTimeTextView = itemView.findViewById(R.id.alarmTimeTextView);
            repeatDaysTextView = itemView.findViewById(R.id.repeatDaysTextView);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
        }
    }
}