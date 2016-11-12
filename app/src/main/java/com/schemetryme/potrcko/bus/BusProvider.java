package com.schemetryme.potrcko.bus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Stefan on 11/4/2016.
 */

public final class BusProvider {

    /*
    private static Bus bus = null;

    public static Bus getInstance(){
        if (bus == null) {
            synchronized (BusProvider.class) {
                bus = new Bus(ThreadEnforcer.ANY);
            }
        }
        return bus;
    }

    private BusProvider(){

    }*/
    private static final Bus bus = new MainThreadBus();

    public static Bus getInstance(){
        return bus;
    }

    private BusProvider(){
        //Don't allow public instantiation
    }

    /**
     * Be able to post from any thread to main thread
     */
    public static class MainThreadBus extends Bus {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainThreadBus.super.post(event);
                    }
                });
            }
        }
    }
}
