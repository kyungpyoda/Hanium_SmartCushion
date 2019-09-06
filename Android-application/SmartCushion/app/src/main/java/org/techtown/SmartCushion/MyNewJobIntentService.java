package org.techtown.SmartCushion;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationManagerCompat;

public class MyNewJobIntentService extends JobIntentService {
    private static int NOTIFICATION_ID;

    public MyNewJobIntentService(){
        super();
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        NOTIFICATION_ID = intent.getIntExtra("nid", 0);
        String title = "";
        String msg = "";
        if (getNotificationId() == 1 ) {
            title = "점심먹고 왔니?";
            msg = "일 하기 전에 스트레칭 하자!";
        }
        else if (getNotificationId() == 2) {
            title = "잘 자 :)";
            msg = "자세 바르게 누워서, 잘 자 :)";
        }
        else {
            title = "푸시알림 오류";
            msg = "푸시알림 오류";
        }
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(R.drawable.logo);
        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
    }
}
