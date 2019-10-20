package org.techtown.SmartCushion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class MyReceiver extends BroadcastReceiver {
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    public MyReceiver(){

    }

    ////푸시알림 설정 화면에서 푸시알림을 on 했을 때 intent를 받아서 알림 세부 설정을 위해 intent를 Broadcast
    @Override
    public void onReceive(Context context, Intent intent) {
        powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "WAKELOCK");
        wakeLock.acquire();
        wakeLock.release();
        int nid = intent.getIntExtra("nid", 0);
        Intent intent1 = new Intent(context, MyNewJobIntentService.class);
        intent1.putExtra("nid", nid);
        context.startService(intent1);
    }

}
