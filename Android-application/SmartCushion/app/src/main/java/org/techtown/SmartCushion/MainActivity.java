package org.techtown.SmartCushion;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.techtown.SmartCushion.Fragment1.status_img;
import static org.techtown.SmartCushion.Fragment1.text_status;

public class MainActivity extends AppCompatActivity {
    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;
    Fragment4 fragment4;
    Fragment5 fragment5;
    static String USERID;
    static String USERNAME;
    public static boolean inGame;

    public static MqttAndroidClient mqttAndroidClient;

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MqttConnecting();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MY KEY HASH:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        Log.d("TEST", "mainoncreate");
        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();
        fragment4 = new Fragment4();
        fragment5 = new Fragment5();

        inGame = false;

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();


        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        String currentTab = getSupportFragmentManager().findFragmentById(R.id.container).toString().substring(0,9);
                        switch (currentTab) {
                            case "Fragment1":
                                try {
                                    mqttAndroidClient.unsubscribe("cushion");
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "Fragment4":
                                break;
                            case "Fragment5":
                                break;
                        }
                        switch (item.getItemId()) {
                            case R.id.tab1:
                                //Toast.makeText(getApplicationContext(), "첫 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
                                try {
                                    mqttAndroidClient.subscribe("cushion", 0);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            case R.id.tab2:
                                //Toast.makeText(getApplicationContext(), "두 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).commit();
                                return true;
                            case R.id.tab3:
                                //Toast.makeText(getApplicationContext(), "세 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment3).commit();
                                return true;
                            case R.id.tab4:
                                //Toast.makeText(getApplicationContext(), "네 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment4).commit();
                                return true;
                            case R.id.tab5:
                                //Toast.makeText(getApplicationContext(), "다섯 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment5).commit();
                                return true;
                                default:

                            //TODO:프래그먼트 수정
                        }
                        return false;
                    }
                });
        Intent intent = getIntent();
        ArrayList<String> ss = intent.getStringArrayListExtra("inf");
        Log.d("TEST",ss.get(0));
        Log.d("TEST",ss.get(1));
        Log.d("TEST",ss.get(2));
        USERID = ss.get(0);
        USERNAME = ss.get(1);
        Toast.makeText(this, USERNAME+"님 안녕하세요.", Toast.LENGTH_SHORT).show();

    }

    public void onTabSelected(int position) {
        if (position == 0) {
            bottomNavigation.setSelectedItemId(R.id.tab1);
        } else if (position == 1) {
            bottomNavigation.setSelectedItemId(R.id.tab2);
        } else if (position == 2) {
            bottomNavigation.setSelectedItemId(R.id.tab3);
        } else if (position == 3) {
            bottomNavigation.setSelectedItemId(R.id.tab4);
        } else if (position == 4) {
            bottomNavigation.setSelectedItemId(R.id.tab5);
        }
    }

    public void MqttConnecting() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),  "tcp://169.56.84.167:1883", MqttClient.generateClientId());
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());     //mqtttoken 이라는것을 만들어 connect option을 달아줌
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                    mqttCallbackSetting();
                    Log.e("Connect_success", "Success");

                    //로그인 하자마자 바로 페어링 시도
                    try {
                        mqttAndroidClient.subscribe("accepted", 0);
                        mqttAndroidClient.subscribe("cushion",0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    try { //서버로 userid 보냄
                        mqttAndroidClient.publish("pairing", USERID.getBytes(), 0 , false );
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    text_status.setText("자세 정보 없음");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                    Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (
                MqttException e)
        {
            e.printStackTrace();
        }

    }
    public void mqttCallbackSetting() {
        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {    //모든 메시지가 올때 Callback method
                Log.e("Connect_success", "messageArrived : "+message);
                String msg = new String(message.getPayload());

                for(int i=0;i<msg.length();i++){
                    Log.e("Connect_success", String.valueOf(msg.charAt(i)));
                }
                //msg=msg.substring(0,msg.length()-1);
                Log.e("Connect_success", msg+", length : "+msg.length());

                switch (topic) {
                    case "cushion":
                        if(inGame) {
                            Fragment4_1.carMove(Integer.parseInt(msg));
                            break;
                        }
                        if(msg.equals("0")) {
                            Log.e("AM", msg);
                            text_status.setText("정 상");
                            status_img.setImageDrawable(getResources().getDrawable(R.drawable.cushion_good));
                        }
                        else if(msg.equals("-1")) {
                            Log.e("AM", msg);
                            text_status.setText("자세 정보 없음");
                            status_img.setImageDrawable(getResources().getDrawable(R.drawable.cushion));
                        }
                        else {
                            Log.e("AM", msg);
                            status_img.setImageDrawable(getResources().getDrawable(R.drawable.cushion_bad));
                            //임시로 1, 2 좌우 바꿔놈
                            if(msg.equals("2")) text_status.setText("좌로 쏠림");
                            else if(msg.equals("1")) text_status.setText("우로 쏠림");
                            else if(msg.equals("3")) text_status.setText("앞으로 쏠림");
                            else if(msg.equals("4")) text_status.setText("뒤로 쏠림");

                        }
                        break;

                    case "accepted":
                        switch (msg) {
                            case "Accepted":
                                try {
                                    mqttAndroidClient.subscribe("cushion", 0);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                mqttAndroidClient.unsubscribe("accepted");
                                break;
                            case "Denied":
                                break;
                        }
                        break;

                    case "setting":
                        if(msg.equals("2")) {

                        }
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
}