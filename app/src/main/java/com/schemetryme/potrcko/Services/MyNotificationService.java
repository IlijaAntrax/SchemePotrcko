package com.schemetryme.potrcko.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

<<<<<<< HEAD
import com.schemetryme.potrcko.LocalServices.Notifications;
import com.schemetryme.potrcko.MainActivity;
=======
import com.schemetryme.potrcko.activities.MainActivity;
>>>>>>> login_and_signup
import com.schemetryme.potrcko.R;
import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MyNotificationService extends Service {

    protected Bus mBus = BusProvider.getInstance();
    private boolean mIsInForegroundMode;
    private final static int MY_ID = 539;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
    }

    @Subscribe
    public void foregroundMode(Boolean mIsInForegroundMode){
        this.mIsInForegroundMode = mIsInForegroundMode;
    }

    @Subscribe
    public void myNotifications(Notifications notifi){
        if(!mIsInForegroundMode){
            List<JSONObject> notifications = notifi.getNotificationsList();
            for(JSONObject obj : notifications){
                try {
                    if(!mIsInForegroundMode)
                        setNotification(obj);
                }catch (JSONException e){
                    e.getStackTrace();
                }

            }
        }
    }

    // TODO: 11/9/2016 Odraditi obavestenje,dodati intetnt koji se otvara, proveriti vise obavestenja

    private void setNotification(JSONObject notificaion) throws JSONException {


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_search_map)
                        .setContentTitle(notificaion.getString("messageType"))
                        .setContentText(notificaion.getString("message"))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // TODO: 11/9/2016 Svakoj notifikaciji dodati drugi contentText, drugi Bundle i podatke

        switch (notificaion.getString("messageType")){
            case Notifications.MSG_DATA : {
                Bundle b = new Bundle();
                JSONObject data = notificaion.getJSONObject("message");
                b.putString("user", data.getJSONObject("fromUser").toString());

                break;
            }
            case Notifications.MSG_ACCEPT : {
                break;
            }
            case Notifications.MSG_END_LOCATION : {
                break;
            }
            case  Notifications.MSG_START_LOCATION : {
                break;
            }
            case Notifications.MSG_NEW_RATE : {
                break;
            }

        }

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