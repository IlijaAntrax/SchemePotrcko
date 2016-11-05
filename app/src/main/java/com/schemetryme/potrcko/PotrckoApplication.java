package com.schemetryme.potrcko;

import android.app.Application;
import android.content.Intent;

import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;

import java.lang.Thread;


/**
 * Created by Stefan on 11/4/2016.
 */

public class PotrckoApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), MyService.class));
            }
        });

    }
}
