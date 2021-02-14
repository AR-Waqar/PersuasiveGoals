package com.example.goals;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class OAGDbHelper extends SQLiteOpenHelper{

    public OAGDbHelper(Context context) {
        super(context, "persuasiveLiteX.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // db.execSQL("create table person (rowId integar primary key, did,day,week,month,year,hours,minutes,TAGtoday,TAGtodayGoal,SAGtoday,SAGyesterday,OAGtoday,OAGother)");
     /*   db.execSQL("create table tag (rowId integar primary key, did,day,week,month,year,hours,minutes,TAGtoday,TAGtodayGoal)");
        db.execSQL("create table sag (rowId integar primary key, did,day,week,month,year,hours,minutes,SAGtoday,SAGyesterday)");
        db.execSQL("create table oag (rowId integar primary key, did,day,week,month,year,hours,minutes,OAGtoday,OAGothers)");*/

        db.execSQL("create table tag (did,date,time,TAGtoday,TAGtodayGoal)");
        db.execSQL("create table sag (did,date,time,SAGtoday,SAGyesterday)");
        db.execSQL("create table oag (did,date,time,OAGtoday,OAGothers)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists tag");
        db.execSQL("drop table if exists sag");
        db.execSQL("drop table if exists oag");
        onCreate(db);
    }

}
