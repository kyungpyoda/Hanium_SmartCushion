package org.techtown.SmartCushion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int nid = intent.getIntExtra("nid", 0);
        Intent intent1 = new Intent(context, MyNewJobIntentService.class);
        intent1.putExtra("nid", nid);
        context.startService(intent1);
    }
}
