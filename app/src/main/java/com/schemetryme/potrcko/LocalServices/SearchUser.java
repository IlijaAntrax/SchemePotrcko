package com.schemetryme.potrcko.LocalServices;

/**
 * Created by Stefan on 12/25/2016.
 */

public class SearchUser {
    boolean get;
    String data;

    public SearchUser(boolean get, String data){
        this.get = get;
        this.data = data;
    }

    public boolean isGet(){return get;}
    public String getData(){return data;}
}
