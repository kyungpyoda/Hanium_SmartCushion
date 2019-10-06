package org.techtown.SmartCushion;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import static org.techtown.SmartCushion.MainActivity.mqttAndroidClient;

public class Fragment5_1 extends Fragment {
    ArrayList<TextView> ps;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment5_1, container, false);
        ps = new ArrayList<>();
        ps.add((TextView) rootView.findViewById(R.id.p1));
        ps.add((TextView) rootView.findViewById(R.id.p2));
        ps.add((TextView) rootView.findViewById(R.id.p3));
        ps.add((TextView) rootView.findViewById(R.id.p4));
        ps.add((TextView) rootView.findViewById(R.id.p5));
        ps.add((TextView) rootView.findViewById(R.id.p6));
        ps.add((TextView) rootView.findViewById(R.id.p7));
        ps.add((TextView) rootView.findViewById(R.id.p8));
        ps.add((TextView) rootView.findViewById(R.id.p9));
        ps.add((TextView) rootView.findViewById(R.id.p10));
        ps.add((TextView) rootView.findViewById(R.id.p11));
        ps.add((TextView) rootView.findViewById(R.id.p12));
        ps.add((TextView) rootView.findViewById(R.id.p13));
        ps.add((TextView) rootView.findViewById(R.id.p14));
        ps.add((TextView) rootView.findViewById(R.id.p15));
        ps.add((TextView) rootView.findViewById(R.id.p16));
        ps.add((TextView) rootView.findViewById(R.id.p17));
        ps.add((TextView) rootView.findViewById(R.id.p18));
        ps.add((TextView) rootView.findViewById(R.id.p19));
        ps.add((TextView) rootView.findViewById(R.id.p20));
        ps.add((TextView) rootView.findViewById(R.id.p21));
        ps.add((TextView) rootView.findViewById(R.id.p22));
        ps.add((TextView) rootView.findViewById(R.id.p23));
        ps.add((TextView) rootView.findViewById(R.id.p24));
        ps.add((TextView) rootView.findViewById(R.id.p25));
        ps.add((TextView) rootView.findViewById(R.id.p26));
        ps.add((TextView) rootView.findViewById(R.id.p27));
        ps.add((TextView) rootView.findViewById(R.id.p28));
        ps.add((TextView) rootView.findViewById(R.id.p29));
        ps.add((TextView) rootView.findViewById(R.id.p30));
        ps.add((TextView) rootView.findViewById(R.id.p31));

        try {
            mqttAndroidClient.subscribe("cushion",0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            mqttAndroidClient.subscribe("setting", 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                if(topic.equals("cushion")) {
                    Log.e("Connect_success",msg);
                    if(msg.equals("9")) {
                        int temp;
                        for(int i = 0; i < ps.size(); i++) {
                            temp = Integer.parseInt(ps.get(i).getText().toString());
                            temp++;
                            ps.get(i).setText(Integer.toString(temp));
                            ps.get(i).setBackgroundColor(Color.rgb(255,255-temp,255-temp));
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        initUI(rootView);


        return rootView;
    }
    private void initUI(ViewGroup rootView) {

        Button testbtn = rootView.findViewById(R.id.button2);
        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                int temp;
                for(int i = 0; i < ps.size(); i++) {
                    temp = Integer.parseInt(ps.get(i).getText().toString());
                    temp++;
                    ps.get(i).setText(Integer.toString(temp));
                    ps.get(i).setBackgroundColor(Color.rgb(255,255-temp,255-temp));
                 }
                */
                try {
                    mqttAndroidClient.publish("cushion","9".getBytes(),0,false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        Button exit = rootView.findViewById(R.id.button3);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeSetting();
            }
        });
    }
    private void completeSetting() {
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onPause() {
        try {
            mqttAndroidClient.publish("setting", "1".getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onPause();
    }
}
