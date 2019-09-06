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

import static org.techtown.SmartCushion.MainActivity.USERNAME;

public class Fragment5 extends Fragment {
    Switch switch_lunch;
    Switch switch_b4sleep;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment5, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(ViewGroup rootView) {
        Button button = rootView.findViewById(R.id.button8);
        button.setText("   " + USERNAME + " 님   >");
        button.setOnClickListener(new View.OnClickListener() {
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

        final SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
        final SharedPreferences.Editor editor = prefs.edit();

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
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis()+5000,
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
                    Toast.makeText(getActivity(), "점심시간 스트레칭 알림 OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis()+5000,
                            1000*60, pendingIntent);
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
}
