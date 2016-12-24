package com.schemetryme.potrcko.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.schemetryme.potrcko.activities.MainActivity;
import com.schemetryme.potrcko.R;

public class MyNotificationService extends Service {

    private final static int MY_ID = 539;

    public MyNotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // TODO: 11/9/2016 Odraditi obavestenje,dodati intetnt koji se otvara, proveriti vise obavestenja

    private void setNotification(String question){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_search_map)
                        //.setContentTitle(getString(R.string.text_obavestenja))
                        .setContentText(question)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(MY_ID, mBuilder.build());
    }
}
