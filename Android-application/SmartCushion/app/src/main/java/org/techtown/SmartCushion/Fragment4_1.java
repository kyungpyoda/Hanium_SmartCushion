package org.techtown.SmartCushion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Fragment4_1 extends Fragment {
    GameView v;
    Fragment4 fragment4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment4_1, container, false);
        Log.d("test","fragment4-1");
        //initUI(rootView);
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

    @Override
    public void onPause() {
        Log.d("test","pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("test","stop");

        super.onStop();
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
        }

        @Override
        protected void onDraw(Canvas canvas) {
            viewWidth = this.getMeasuredWidth();
            viewHeight = this.getMeasuredHeight();

            if (time % 700 < 6 + 2 * speed) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("lane", rand.nextInt(3));
                map.put("startTime", time);
                otherCars.add(map);
            }

            time = time + 6 + 2 * speed;

            int carWidth = viewWidth / 5;
            int carHeight = carWidth + 10;

            myPaint.setStyle(Paint.Style.FILL);

            myPaint.setColor(Color.RED);
            canvas.drawRect((myCarPosition * viewWidth / 3) + viewWidth / 15, viewHeight - 2 - carHeight, (myCarPosition * viewWidth / 3) + (viewWidth / 15) + carWidth, viewHeight - 2, myPaint);

            myPaint.setColor(Color.GREEN);

            for (int i = 0; i < otherCars.size(); i++) {
                int carX = ((int) otherCars.get(i).get("lane") * viewWidth / 3) + viewWidth / 15;
                int carY = time - (int) otherCars.get(i).get("startTime");

                canvas.drawRect(carX, carY - carHeight, carX + carWidth, carY, myPaint);

                if ((int) otherCars.get(i).get("lane") == myCarPosition) {
                    if (carY > viewHeight - 2 - carHeight && carY < viewHeight - 2) {
                        prefs.edit().putInt("highscore", highscore).commit();
                        fragment4 = new Fragment4();
                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, fragment4)
                                .commit();
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

            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(40);
            canvas.drawText("Score: " + String.valueOf(score), 80f, 80f, myPaint);
            canvas.drawText("Speed: " + String.valueOf(speed), 380f, 80f, myPaint);

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float x1 = event.getX();
                    if (x1 < viewWidth / 2) {
                        if (myCarPosition > 0) {
                            myCarPosition--;
                        }
                    }
                    if (x1 > viewWidth / 2) {
                        if (myCarPosition < 2) {
                            myCarPosition++;
                        }
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;

        }
    }
}

