package com.example.liumx.contacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBOpenHandler extends SQLiteOpenHelper {
    private int version;
    public DBOpenHandler(Context context, String name,
                         SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE white_list(_id integer primary key autoincrement," +
                "raw_id int NOT NULL, phone varchar(64) NOT NULL unique)");
        db.execSQL("CREATE TABLE notify_list(_id integer primary key autoincrement," +
                "raw_id int NOT NULL unique, name nvarchar(64), phone varchar(64)," +
                "date_time varchar(64), note nvarchar(64))");
        db.execSQL("CREATE TABLE pref(setting_item nvarchar(64) NOT NULL unique," +
                "data varchar(64))");
        db.execSQL("CREATE TABLE my_card(name nvarchar(64), phone varchar(64)," +
                "email varchar(64), organization varchar(64), address varchar(64)" +
                ", birthday varchar(64))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBOpenHandler", "onUpgrade");
    }
}

