package com.example.goals;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class GoalsDbHelper extends SQLiteOpenHelper {

    
    public GoalsDbHelper(Context context) {
        super(context, "PersuasiveTSO.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //todays goal is the fixed count of 7500
        db.execSQL("create table tag (did,date,reset_value,counter,today_steps,todays_goal)");

        //todays goal is the last step count from yesterday
        db.execSQL("create table sag (did,date,reset_value,counter,today_steps,todays_goal)");

        //todays goal is  the aggregate of others steps
        db.execSQL("create table oag (did,date,reset_value,counter,today_steps,todays_goal)");

        // No goal is involve, Only steps count
        db.execSQL("create table welcome (did,dinfo,date,reset_value,counter,today_steps)");

        // screens
        db.execSQL("create table screens (did,screen,start_date,end_date,status)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists tag");
        db.execSQL("drop table if exists sag");
        db.execSQL("drop table if exists oag");
        db.execSQL("drop table if exists welcome");
        db.execSQL("drop table if exists screens");
        onCreate(db);
    }
}
