package com.example.ycblesdkdemo.configs;

import android.content.Context;
import android.content.SharedPreferences;

public class Auth {
    private SharedPreferences sharedPreferences;
    private Context context;
    private Integer user_id;
    private String fullname;
    private String username;
    private String email;
    private String device_mac;
    private String deviece_name;

    public Auth(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        this.user_id = sharedPreferences.getInt("user_id", 0);
        this.fullname = sharedPreferences.getString("fullname", "");
        this.username = sharedPreferences.getString("username", "");
        this.email = sharedPreferences.getString("email", "");
        this.device_mac = sharedPreferences.getString("device_mac", "");
        this.deviece_name = sharedPreferences.getString("device_name", "");
    }

    public Auth(Integer User_id, String Fullname, String Username, String email, String device_mac, String deviece_name) {
        this.user_id = User_id;
        fullname = Fullname;
        this.fullname = Fullname;
        this.email = email;
        this.device_mac = device_mac;
        this.deviece_name = deviece_name;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public String getDeviece_name() {
        return deviece_name;
    }

    public void setDeviece_name(String deviece_name) {
        this.deviece_name = deviece_name;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void logout() {
        sharedPreferences = this.context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", 0);
        editor.putString("fullname", "");
        editor.putString("username", "");
        editor.putString("email", "");
        editor.putString("device_mac", "");
        editor.putString("device_name", "");
        editor.commit();
    }
}
