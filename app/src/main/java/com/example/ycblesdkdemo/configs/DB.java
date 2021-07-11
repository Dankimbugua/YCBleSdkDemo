package com.example.ycblesdkdemo.configs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE = "db_iot_medical.db";
    private static final String TABLE_HISTORY = "history";
    private static final String TABLE_LOG = "log";

    public DB(Context context) {
        super(context, DATABASE, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date NUMERIC NOT NULL," +
                "oxygen NUMERIC NOT NULL," +
                "temperature NUMERIC NOT NULL," +
                "hrv NUMERIC NOT NULL," +
                "cvrr NUMERIC NOT NULL," +
                "resp_rate NUMERIC NOT NULL," +
                "status NUMERIC NOT NULL)"
        );
        db.execSQL("CREATE TABLE " + TABLE_LOG + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date NUMERIC NOT NULL," +
                "device_con NUMERIC NOT NULL," +
                "status NUMERIC NOT NULL)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
    }
}
