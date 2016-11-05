package com.schemetryme.potrcko.bus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Stefan on 11/4/2016.
 */

public final class BusProvider {
    private static Bus bus = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance(){
        return bus;
    }

    private BusProvider(){

    }
}
