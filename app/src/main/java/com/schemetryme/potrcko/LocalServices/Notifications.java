package com.schemetryme.potrcko.LocalServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefan on 11/9/2016.
 */

public class Notifications {
    public static final String MSG_DATA = "Data From Route";
    public static final String MSG_ACCEPT = "Accept Job";
    public static final String MSG_RATE_ACCESS = "Rate Access";
    public static final String MSG_NEW_RATE = "New Rate";
    public static final String MSG_START_LOCATION = "Start Notification";
    public static final String MSG_END_LOCATION = "End Notification";

    List<JSONObject> notifications = new ArrayList<>();
    User user = new User();

    public Notifications(String s) throws JSONException{
        JSONArray array = new JSONArray(s);
        int i = 0;
        while(i++ != array.length()){
            try{
                notifications.add(array.getJSONObject(i));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public List<JSONObject> getNotificationsList(){
        return notifications;
    }
}
