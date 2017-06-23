package com.schemetryme.potrcko.LocalServices;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.schemetryme.potrcko.MainActivity;

/**
 * Created by Stefan on 11/5/2016.
 */

public class MyLocalService {

    public static final String PREF_USER_USER = "user";
    public static final String PREF_USER_TOKEN = "token";
<<<<<<< HEAD
    public static final String URL = "http://192.168.1.6:3000/";
=======
    public static final String URL = "http://109.92.116.89:3000/";
>>>>>>> login_and_signup

    public static User user;
    public static String token;


    private static MyLocalService sInstance;

    public static User getUser(){return user;}
    public static String getToken(){return token;}

    private MyLocalService(){}

    public static MyLocalService getInstance() {
        if (sInstance == null) {
            synchronized (MyLocalService.class) {
                sInstance = new MyLocalService();
            }
        }
        return sInstance;
    }

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setLogin(Context ctx, User user, String token)
    {
        this.user = user;
        this.token = token;
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_USER, user.toString());
        editor.putString(PREF_USER_TOKEN, token);
        editor.commit();
    }

    public Boolean getLogin(Context ctx)
    {
        String usr = getSharedPreferences(ctx).getString(PREF_USER_USER, "");
        String tkn = getSharedPreferences(ctx).getString(PREF_USER_TOKEN, "");

        if(usr != "" && tkn != ""){
            user = new User();
            user.setUser(usr);
            token = tkn;
            return true;
        }else
            return false;
    }
}
