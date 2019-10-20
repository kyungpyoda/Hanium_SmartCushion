package org.techtown.SmartCushion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.eclipse.paho.client.mqttv3.MqttException;

import static org.techtown.SmartCushion.MainActivity.mqttAndroidClient;

public class Fragment4 extends Fragment {
    Fragment4_1 fragment4_1;
    TextView tv_hs;
    Button gameStart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment4, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(ViewGroup rootView) {
        final SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
        int hs = prefs.getInt("highscore", 0);

        tv_hs = rootView.findViewById(R.id.tv_hs);
        tv_hs.setText("최고 점수 : " + Integer.toString(hs));
        Log.d("test","fragment4");

        ImageView imgv1 = (ImageView)rootView.findViewById(R.id.imgv1);
        Glide.with(rootView).load(R.drawable.gamemain3).into(imgv1);
        ImageView imgv2 = (ImageView)rootView.findViewById(R.id.imgv2);
        Glide.with(rootView).load(R.drawable.gamemain2).into(imgv2);

        ////게임 시작 버튼
        gameStart = rootView.findViewById(R.id.gameStart);
        gameStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment4_1 = new Fragment4_1();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment4_1)
                        .addToBackStack(null)
                        .commit();
                try {
                    mqttAndroidClient.subscribe("cushion",0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("test","Frag4_resume");
        try {
            mqttAndroidClient.unsubscribe("cushion");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
