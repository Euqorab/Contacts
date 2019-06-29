package com.example.liumx.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Euqorab on 2019/6/21.
 */

public class ContactDb {
    private DBOpenHandler dbOpenHandler;

    public ContactDb(Context context) {
        this.dbOpenHandler = new DBOpenHandler(context, "dbContact.db3", null, 1);
    }

    public void insert(String table, ContentValues cv) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        db.insert(table, null, cv);
        db.close();
    }

    public int delete(String table, String where, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        int ret = db.delete(table, where, whereArgs);
        db.close();
        return ret;//影响的记录数
    }

    public int update(String table, ContentValues cv, String where, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        int ret = db.update(table, cv, where, whereArgs);
        db.close();
        return ret; //影响的记录数
    }

    public Cursor query(String table, String[] projection, String where,
                        String[] whereArgs, String sortOrder){
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        Cursor cursor = db.query(table, projection, where, whereArgs, null, null, sortOrder, null);
        return cursor;
    }

    public long getCount(String table) {//得到记录总数
        SQLiteDatabase db = dbOpenHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + table, null);
        cursor.moveToFirst();
        db.close();
        return cursor.getLong(0);
    }
}
