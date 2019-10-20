package org.techtown.SmartCushion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.eclipse.paho.client.mqttv3.MqttException;

import static org.techtown.SmartCushion.MainActivity.USERNAME;
import static org.techtown.SmartCushion.MainActivity.mqttAndroidClient;

public class Fragment5 extends Fragment {
    Switch switch_lunch;
    Switch switch_b4sleep;
    Switch switch_live;
    Fragment5_1 fragment5_1;
    //SettingTask settingTask;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment5, container, false);

        initUI(rootView);

        return rootView;
    }

    private void initUI(ViewGroup rootView) {
        //데이터 앱 내 저장, 불러오기 위한 SharedPreference
        final SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
        final SharedPreferences.Editor editor = prefs.edit();

        ////카카오 계정 로그아웃 버튼
        Button button_logout = rootView.findViewById(R.id.button_logout);
        button_logout.setText("   " + USERNAME + " 님   >");
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
                alt_bld.setMessage("로그아웃 하시겠습니까?").setCancelable(
                        false).setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Action for 'Yes' Button
                                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                                    @Override
                                    public void onCompleteLogout() {
                                        final Intent intent = new Intent(getContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                // Title for AlertDialog
                //alert.setTitle("로그아웃");
                // Icon for AlertDialog
                //alert.setIcon(R.drawable.logo);
                alert.show();
            }
        });
        Button button_setting = rootView.findViewById(R.id.button_setting);
        button_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingDialog settingDialog = new SettingDialog(getActivity());
                settingDialog.callFunction(prefs.getString("setData",""));
            }
        });


        switch_live = rootView.findViewById(R.id.switch_live);
        boolean is_switch_live_checked = prefs.getBoolean("switch_live", false);
        switch_live.setChecked(is_switch_live_checked);
        switch_live.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("switch_live", isChecked);
                editor.commit();
                //실시간 자세 교정 피드백 on/off 방석으로 전달
                if(isChecked) {
                    try { //진동 활성화
                        mqttAndroidClient.publish("setting", "4".getBytes(),0,false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try { //진동 비활성화
                        mqttAndroidClient.publish("setting", "3".getBytes(),0,false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ////////점심시간 전 스트레칭 푸시알림 설정
        switch_lunch = rootView.findViewById(R.id.switch_lunch);
        boolean is_switch_lunch_checked = prefs.getBoolean("switch_lunch", false);
        switch_lunch.setChecked(is_switch_lunch_checked);
        switch_lunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("switch_lunch", isChecked);
                editor.commit();

                if (isChecked) {
                    Intent notifyIntent = new Intent(getActivity(), MyReceiver.class);
                    notifyIntent.putExtra("nid",1);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast
                            (getActivity(), 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    ////triggertime은 오후 1시 10분 전, 주기는 24시간
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000,
                            1000*60, pendingIntent);
                    Toast.makeText(getActivity(), "점심시간 스트레칭 알림 ON", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent notifyIntent = new Intent(getActivity(), MyReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getActivity(), 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                    Toast.makeText(getActivity(), "점심시간 스트레칭 알림 OFF ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ////잠 자기 전 푸시알림 설정
        switch_b4sleep = rootView.findViewById(R.id.switch_b4sleep);
        boolean is_switch_b4sleep_checked = prefs.getBoolean("switch_b4sleep", false);
        switch_b4sleep.setChecked(is_switch_b4sleep_checked);
        switch_b4sleep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("switch_b4sleep", isChecked);
                editor.commit();

                if (isChecked) {
                    Intent notifyIntent = new Intent(getActivity(), MyReceiver.class);
                    notifyIntent.putExtra("nid",2);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast
                            (getActivity(), 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    ////triggertime은 밤 11시 50분, 주기는 24시간
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  1000*60*60*24-1000*60*10,
                            1000*60*60*24, pendingIntent);
                    Toast.makeText(getActivity(), "잠 자기 전 알림 ON", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent notifyIntent = new Intent(getActivity(), MyReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getActivity(), 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                    Toast.makeText(getActivity(), "잠 자기 전 알림 OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ////초기값 측정을 위한 로딩 다이얼로그
    /*
    class SettingTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("측정중입니다... \n바른 자세를 유지해주세요. \n(약 10초 소요)");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for(int i=0;i<5;i++){
                    asyncDialog.setProgress(i*30);
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }
    */
}
