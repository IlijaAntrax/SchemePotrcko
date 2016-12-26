package com.schemetryme.potrcko.Search.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefan on 12/25/2016.
 */

public class UserJSONParser {

    public List<HashMap<String, String>> parse(JSONObject jObject) throws Exception {

        JSONArray array = null;
        try {
            boolean success = jObject.getBoolean("success");
            if(!success) throw new Exception(jObject.getString("message"));
            else
                array = jObject.getJSONArray("message");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return getUsers(array);
    }

    private List<HashMap<String, String>> getUsers(JSONArray jUsers) throws Exception{
        // TODO: 12/25/2016 Odraditi obradu dobijenih podataka koje se dobiju kao niz od servera

        List<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> place;

        for(int i=0; i<jUsers.length();i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getUser((JSONObject) jUsers.get(i));
                list.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    private HashMap<String,String> getUser(JSONObject user){
        HashMap<String,String> usr = new HashMap<String,String>();
        try{
            usr.put("name", user.getString("firstname") + " " + user.getString("lastname"));
            usr.put("email", user.getString("email"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return usr;
    }

}
