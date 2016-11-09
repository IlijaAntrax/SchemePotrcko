package com.schemetryme.potrcko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.schemetryme.potrcko.Services.MyLocationService;
import com.schemetryme.potrcko.Services.MySocketService;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;

/**
 * Created by Stefan on 11/9/2016.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if(!isConnected(context))
            Toast.makeText(context, "Lost connect.", Toast.LENGTH_LONG).show();

        serviceOption(isConnected(context), context);
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

    public void serviceOption(boolean startService,final Context ctx){
        if(startService){
            //Start location services
            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    ctx.startService(new Intent(ctx, MyLocationService.class));
                }
            });

            //Strat socket service
            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    ctx.startService(new Intent(ctx, MySocketService.class));
                }
            });
        }else{
            ctx.stopService(new Intent(ctx, MyLocationService.class));
            ctx.stopService(new Intent(ctx, MySocketService.class));
        }
    }
}