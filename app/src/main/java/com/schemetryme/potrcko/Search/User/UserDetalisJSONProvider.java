package com.schemetryme.potrcko.Search.User;

import com.schemetryme.potrcko.LocalServices.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stefan on 12/26/2016.
 */

public class UserDetalisJSONProvider {

    public User parse(String users) throws Exception{

        String user = "";
        JSONObject res;
        JSONArray array;
        JSONObject usr;
        try{
            res = new JSONObject(users);

            if(res.getBoolean("success")) {
                array = res.getJSONArray("message");
                usr = array.getJSONObject(0);
                user = usr.toString();
            }
            else
                throw new Exception(res.getString("message"));
        }catch (JSONException e){
            e.printStackTrace();
        }

        return getUser(user);
    }

    private User getUser(String user){
        User u = new User();
        u.setUser(user);
        return u;
    }
}
