package com.schemetryme.potrcko;

import android.app.Application;
import android.content.Intent;

import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.Services.MySocketService;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;


/**
 * Created by Stefan on 11/4/2016.
 */

public class PotrckoApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        User user = new User("123", "Stefan", "Stankovic","stefan@stankovic@gmail.com", "Thu Dec 29 2011 20:14:56 GMT-0600 (CST)",
                "Thu Dec 29 2011 20:14:56 GMT-0600 (CST)", "06123456789", true, false, 5.0);

        if(!MyLocalService.getInstance().getLogin(this))
            MyLocalService.getInstance().setLogin(this, user, "token");

    }
}
