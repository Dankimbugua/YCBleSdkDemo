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

    public Auth(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        this.user_id = sharedPreferences.getInt("user_id", 0);
        this.fullname = sharedPreferences.getString("fullname", "");
        this.username = sharedPreferences.getString("username", "");
        this.email = sharedPreferences.getString("email", "");
    }

    public Auth(Integer User_id, String Fullname, String Username, String email) {
        this.user_id = User_id;
        fullname = Fullname;
        this.fullname = Fullname;
        this.email = email;
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
        this.user_id = sharedPreferences.getInt("user_id", 0);
        this.fullname = sharedPreferences.getString("fullname", "");
        this.username = sharedPreferences.getString("username", "");
        this.email = sharedPreferences.getString("email", "");
        editor.commit();
    }
}
