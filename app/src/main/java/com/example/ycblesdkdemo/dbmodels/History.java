package com.example.ycblesdkdemo.dbmodels;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.ycblesdkdemo.configs.DB;

import java.util.ArrayList;

public class History {
    private DB instance;

    private String date;
    private Integer id,user_id,oxygen,hrv,cvrr,resp_rate;
    private Double temperature;
    private boolean status;

    public History(Context context) {
        this.instance = new DB(context);
    }

    public History(Context context,
                   Integer id,
                   Integer user_id,
                   String date,
                   Integer oxygen,
                   Double temperature,
                   Integer hrv,
                   Integer cvrr,
                   Integer resp_rate,
                   boolean status) {
        this.instance = new DB(context);
        this.date = date;
        this.id = id;
        this.user_id = user_id;
        this.oxygen = oxygen;
        this.hrv = hrv;
        this.cvrr = cvrr;
        this.resp_rate = resp_rate;
        this.temperature = temperature;
        this.status = status;
    }

    public History(Integer id,
                   Integer user_id,
                   String date,
                   Integer oxygen,
                   Double temperature,
                   Integer hrv,
                   Integer cvrr,
                   Integer resp_rate,
                   boolean status) {
        this.date = date;
        this.id = id;
        this.user_id = user_id;
        this.oxygen = oxygen;
        this.hrv = hrv;
        this.cvrr = cvrr;
        this.resp_rate = resp_rate;
        this.temperature = temperature;
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

    public Integer getOxygen() {
        return oxygen;
    }

    public void setOxygen(Integer oxygen) {
        this.oxygen = oxygen;
    }

    public Integer getHrv() {
        return hrv;
    }

    public void setHrv(Integer hrv) {
        this.hrv = hrv;
    }

    public Integer getCvrr() {
        return cvrr;
    }

    public void setCvrr(Integer cvrr) {
        this.cvrr = cvrr;
    }

    public Integer getResp_rate() {
        return resp_rate;
    }

    public void setResp_rate(Integer resp_rate) {
        this.resp_rate = resp_rate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
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
            values.put("oxygen",oxygen);
            values.put("temperature",temperature);
            values.put("hrv",hrv);
            values.put("cvrr",cvrr);
            values.put("resp_rate",resp_rate);
            values.put("status",status);
            db.insert("history",null,values);
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
            db.update("history", contentValues, "id = ?", new String[]{String.valueOf(id)});
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
    public ArrayList<History> getUnsyncedNames(Context context) {
        this.instance = new DB(context);
        ArrayList<History> arrayListHistory = new ArrayList<>();
        SQLiteDatabase db = instance.getReadableDatabase();
        String sql = "SELECT * FROM history WHERE status = 0";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()){
            do {
                arrayListHistory.add(new History(
                        c.getInt(0),
                        c.getInt(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getDouble(4),
                        c.getInt(5),
                        c.getInt(6),
                        c.getInt(7),
                        Boolean.parseBoolean(c.getString(8))));
            }while(c.moveToNext());
        }
        return arrayListHistory;
    }
}
