package com.schemetryme.potrcko.LocalServices;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Stefan on 11/5/2016.
 */

public class User {
    private String _id;
    private String firstname;
    private String lastname;
    private String email;
    private String birtday;
    private String req_date;
    private String mob_num;
    private Boolean potrcko;
    private Boolean busy;
    private double radius;

    public User(){

    }

    public User(String _id,
                String firstname,
                String lastname,
                String email,
                String birtday,
                String reg_date,
                String mob_num,
                Boolean potrcko,
                Boolean busy,
                double radius){
        this._id = _id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.birtday = birtday;
        this.req_date = reg_date;
        this.mob_num = mob_num;
        this.potrcko = potrcko;
        this.busy = busy;
        this.radius = radius;
    }

    public String toString(){
        try {
            JSONObject obj = new JSONObject();
            obj.put("_id", _id);
            obj.put("firstname", this.firstname);
            obj.put("lastname",this.lastname);
            obj.put("email",this.email);
            obj.put("birthday",this.birtday);
            obj.put("reg_date",this.req_date);
            obj.put("mob_num",this.mob_num);
            obj.put("potrcko", this.potrcko);
            obj.put("busy", this.busy);
            obj.put("radius", this.radius);
            String ret = obj.toString();
            return ret;
        }catch (Exception e){ }
        return null;
    }

    public void setUser(String str)
    {
        try {
            JSONObject obj = new JSONObject(str);
            this._id = obj.getString("_id");
            this.firstname = obj.getString("firstname");
            this.lastname = obj.getString("lastname");
            this.email = obj.getString("email");
            this.birtday = obj.getString("birtday");
            this.req_date = obj.getString("reg_date");
            this.mob_num = obj.getString("mob_num");
            this.potrcko = obj.getBoolean("potrcko");
            this.busy = obj.getBoolean("busy");
            this.radius = obj.getDouble("radius");
        }catch (Exception e){ }
    }

    public String get_id(){ return _id;}
    public String getFirstname(){return firstname;}
    public String getLastname(){return lastname;}
    public String getEmail(){return email;}
    public String getBirtday(){return birtday;}
    public String getReq_date(){return req_date;}
    public String getMob_num(){return mob_num;}
    public Boolean getPotrcko(){return potrcko;}
    public Boolean getBusy(){return busy;}
    public Double getRadius(){return radius;}
    public void serRadius(double radius){this.radius = radius;}
}
