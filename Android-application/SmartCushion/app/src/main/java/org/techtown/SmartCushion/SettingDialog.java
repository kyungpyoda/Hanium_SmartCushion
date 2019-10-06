package org.techtown.SmartCushion;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class SettingDialog {
    private Context context;
    private SettingTask settingTask;

    public SettingDialog(Context context) {
        this.context = context;
    }

    public void callFunction(String setData) {
        //커스텀 다이얼로그 정의하기 위한 dialog 클래스 생성
        final Dialog dlg = new Dialog(context);

        //타이틀바 숨김
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //다이얼로그 레이아웃 설정
        dlg.setContentView(R.layout.setting_dialog);

        //다이얼로그 노출
        dlg.show();

        //다이얼로그 각 위젯 정의
        final TextView tv = dlg.findViewById(R.id.tv);
        final TableLayout tableData = dlg.findViewById(R.id.tableData);
        final Button settingBtn = dlg.findViewById(R.id.settingBtn);
        final Button cancelBtn = dlg.findViewById(R.id.cancelBtn);

        if(setData != null && setData.length() != 0) {
            //저장된 값이 있는 경우
            tv.setVisibility(View.INVISIBLE);
            tableData.setVisibility(View.VISIBLE);
            //TODO : setData를 이용하여 시각화
        }
        else {
            //저장된 값이 없는 경우
            //위젯 default 세팅이 저장된 값이 없는 경우의 세팅이므로 그대로 둠.
        }

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                settingTask = new SettingTask();
                settingTask.execute();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
    }

    ////초기값 측정을 위한 로딩 다이얼로그
    class SettingTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(context);

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
}
