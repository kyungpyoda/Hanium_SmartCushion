package org.techtown.SmartCushion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.techtown.SmartCushion.MainActivity.inGame;
import static org.techtown.SmartCushion.MainActivity.mqttAndroidClient;

public class Fragment4_1 extends Fragment {
    static GameView v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment4_1, container, false);
        Log.d("test","fragment4-1");
        //initUI(rootView);
        //inGame = true;
        ////게임 뷰 생성
        v = new GameView(getActivity());
        return v;
    }
    private void initUI(ViewGroup rootView) {
        GameView bcv = new GameView(getActivity());
        Toast.makeText(getActivity(), "asdf", Toast.LENGTH_SHORT).show();
        Log.d("test","asdf");
        LinearLayout linear1 = rootView.findViewById(R.id.linear1);
        linear1.addView(bcv);
        Log.d("test","return");
    }

    public static void carMove(int lr){
        v.carMove(lr);
    }

    class GameView extends View {
        private Paint myPaint;
        private int speed = 1;
        private int time = 0;
        private int score = 0;
        private int myCarPosition = 0;
        private ArrayList<HashMap<String, Object>> otherCars = new ArrayList<>();
        Random rand = new Random();

        int viewWidth = 0;
        int viewHeight = 0;

        final SharedPreferences prefs = getContext().getSharedPreferences("PREFS", 0);
        int highscore = prefs.getInt("highscore", 0);


        public GameView(Context context) {
            super(context);
            myPaint = new Paint();
            inGame = true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            viewWidth = this.getMeasuredWidth();
            viewHeight = this.getMeasuredHeight();

            canvas.drawColor(0xFF888888);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{100f,30f},0);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setPathEffect(dashPathEffect);
            myPaint.setStrokeWidth(5f);
            myPaint.setColor(0xFFFFE21F);
            Path path = new Path();
            path.moveTo(viewWidth/4,0);
            path.lineTo(viewWidth/4,0);
            path.lineTo(viewWidth/4,viewHeight);
            path.moveTo(viewWidth/4*2,0);
            path.lineTo(viewWidth/4*2,0);
            path.lineTo(viewWidth/4*2,viewHeight);
            path.moveTo(viewWidth/4*3,0);
            path.lineTo(viewWidth/4*3,0);
            path.lineTo(viewWidth/4*3,viewHeight);
            canvas.drawPath(path,myPaint);

            if (time % 1400 < 6 + 2 * speed) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("lane", rand.nextInt(4));
                map.put("startTime", time);
                otherCars.add(map);
            }

            time = time + 6 + 2 * speed;

            int carWidth = viewWidth / 4;
            int carHeight = carWidth + 100;

            myPaint.setStyle(Paint.Style.FILL);

            //myPaint.setColor(Color.RED);
            //canvas.drawRect((myCarPosition * viewWidth / 40), viewHeight - 2 - carHeight, (myCarPosition * viewWidth / 40) + carWidth, viewHeight - 2, myPaint);
            Drawable d;
            d = getResources().getDrawable(R.drawable.car1);
            d.setBounds((myCarPosition * viewWidth / 40), viewHeight - 2 - carHeight, (myCarPosition * viewWidth / 40) + carWidth, viewHeight - 2);
            d.draw(canvas);

            //myPaint.setColor(Color.GREEN);
            for (int i = 0; i < otherCars.size(); i++) {
                int carX = ((int) otherCars.get(i).get("lane") * viewWidth / 4);
                int carY = time - (int) otherCars.get(i).get("startTime");
                int temp = (int)otherCars.get(i).get("lane");
                //canvas.drawRect(carX, carY - carHeight, carX + carWidth, carY, myPaint);
                d = getResources().getDrawable(R.drawable.car2);
                d.setBounds(carX, carY - carHeight, carX + carWidth, carY);
                d.draw(canvas);
                if(carY > viewHeight-carHeight && carY - carHeight < viewHeight) {
                    if((temp-1)*10 < myCarPosition && (temp+1)*10 > myCarPosition){
                        prefs.edit().putInt("highscore", highscore).commit();
                        //fragment4 = new Fragment4();
                        getFragmentManager().popBackStackImmediate();
                    }
                }

                if (carY > viewHeight + carHeight) {
                    otherCars.remove(i);
                    score++;
                    speed = 1 + Math.abs(score / 10);
                    if (score > highscore) {
                        highscore = score;
                    }
                }
            }

            myPaint.setColor(Color.WHITE);
            myPaint.setTextSize(40);
            canvas.drawText("현재 점수: " + String.valueOf(score), 30f, 80f, myPaint);
            canvas.drawText("속도: " + String.valueOf(speed), viewWidth/4+30f, 80f, myPaint);

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float x1 = event.getX();
                    if (x1 < viewWidth / 2) {
                        try {
                            mqttAndroidClient.publish("cushion","1".getBytes(),0,false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    if (x1 > viewWidth / 2) {
                        try {
                            mqttAndroidClient.publish("cushion","2".getBytes(),0,false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;

        }

        public void carMove(int lr) {
            if (lr == 2) {
                if (myCarPosition > 0) {
                    myCarPosition-=10;
                    Log.e("Connect_success", String.valueOf(myCarPosition));
                }
            }
            if (lr == 1) {
                if (myCarPosition < 30) {
                    myCarPosition+=10;
                    Log.e("Connect_success", String.valueOf(myCarPosition));
                }
            }
            invalidate();
        }
    }

    @Override
    public void onPause() {
        MainActivity.inGame = false;
        super.onPause();
    }
}

