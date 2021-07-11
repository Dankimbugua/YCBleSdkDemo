package com.example.ycblesdkdemo.dbmodels;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.ycblesdkdemo.configs.DB;

public class LogH {
    private DB instance;

    private String date;
    private Integer id,user_id;
    private boolean device_con, status;

    public LogH(Context context) {
        this.instance = new DB(context);
    }

    public LogH(Context context, String date, Integer id, Integer user_id, boolean device_con, boolean status) {
        this.instance = new DB(context);
        this.date = date;
        this.id = id;
        this.user_id = user_id;
        this.device_con = device_con;
        this.status = status;
    }

    public LogH(String date, Integer id, Integer user_id, boolean device_con, boolean status) {
        this.date = date;
        this.id = id;
        this.user_id = user_id;
        this.device_con = device_con;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public boolean isDevice_con() {
        return device_con;
    }

    public void setDevice_con(boolean device_con) {
        this.device_con = device_con;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean create(){
        boolean response = false;
        SQLiteDatabase db = instance.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            db.beginTransaction();
            //values.put("id",0);
            values.put("user_id",user_id);
            values.put("date",date);
            values.put("device_con",device_con);
            values.put("status",status);
            db.insert("log",null,values);
            db.setTransactionSuccessful();
            response = true;
        }catch (SQLException e){
            response = false;
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
        return response;
    }

    public boolean updateStatus(int id) {
        boolean response = false;
        SQLiteDatabase db = instance.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", true);
        try {
            db.beginTransaction();
            db.update("log", contentValues, "id = ?", new String[]{String.valueOf(id)});
            //db.close();
            db.setTransactionSuccessful();
            response = true;
        } catch(SQLException e) {
            e.printStackTrace();
            response = false;
        } finally {
            db.endTransaction();
        }
        return response;
    }
}
