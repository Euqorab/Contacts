package com.example.liumx.contacts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 64849 on 2019/6/25.
 */

public class PushNotification extends Service{
    static Timer timer = null;

    public static void cleanAllNotification(Context mcContext) {
        NotificationManager mn = (NotificationManager) mcContext.getSystemService(NOTIFICATION_SERVICE);
        mn.cancelAll();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static void addNotification (Context mContext, int delayTime, String subTitle, String contentTitle, String contentText) {
        Intent intent = new Intent (mContext, PushNotification.class);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("subTitle", subTitle);
        intent.putExtra("contentTitle", contentText);
        intent.putExtra("contentText", contentText);
        Log.e("tryAddNotification", "============Adding=======");
        mContext.startService(intent);
    }

    @Override
    public void onCreate() {
        Log.e("addNotification", "========Create========");
    }

    @Override
    public IBinder onBind (Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        long period = 24*60*60*1000;
        int delay = intent.getIntExtra("delayTime", 0);
        if (null == timer) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager mn = (NotificationManager) PushNotification.this.getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(PushNotification.this);
                Log.e("phone", intent.getStringExtra("phone"));
                Intent notificationIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + intent.getStringExtra("phone")));
                PendingIntent contentIntent = PendingIntent.getActivity(PushNotification.this, 0, notificationIntent, 0);
                builder.setContentIntent(contentIntent);
                builder.setSmallIcon(R.drawable.ic_input_add);
                builder.setTicker(intent.getStringExtra("subTitle"));
                builder.setContentText(intent.getStringExtra("contentText"));
                builder.setContentTitle(intent.getStringExtra("contentTitle"));
                builder.setAutoCancel(true);
                builder.setDefaults(Notification.DEFAULT_ALL);
                builder.setWhen(System.currentTimeMillis());
                Notification notification = builder.build();
                mn.notify(intent.getIntExtra("notificationId", 0), notification);
            }
        }, delay, period);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
