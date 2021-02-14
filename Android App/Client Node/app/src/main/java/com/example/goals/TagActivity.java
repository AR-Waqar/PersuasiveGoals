package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagActivity extends AppCompatActivity implements SensorEventListener, UploadRequestBody.UploadCallback {

    public static final String DB_NAME = "PersuasiveTSO.db";
    TextView TAGStepGoal, TAGStepValue, dummyTV;
    ProgressBar TAGStepGoalCircle, TAGStepValueCircle;
    Integer sTAGStepGoal;Integer yesterday_steps;
    SensorManager sensorManager;
    boolean running = false;
    private View view;
    private String goalsValue;
    //Reads Device ID
    String m_androidId;
    public static final String date_format = "dd-MM-yyyy";
    public SimpleDateFormat date_formatter = new SimpleDateFormat(date_format);
    GoalsDbHelper dbHelper;
    SQLiteDatabase dbWrite;
    SQLiteDatabase dbRead;
    Cursor cursorGoalsDb;

    @Override
    public void onProgressUpdate(int percentage) {

    }


    public class Record {
        public int rowid;
        public String did;
        public Date date;
        public int reset_value;
        public int counter;
        public int today_steps;

        Record(int r,String dev_id,Date d,int a,int b, int c) {
            rowid=r; did=dev_id; reset_value=a; counter=b; today_steps=c; date=d;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TAGStepGoal = (TextView) findViewById(R.id.TAG_Step_Goal);
        TAGStepValue = (TextView) findViewById(R.id.TAG_Step_Value);
       // dummyTV = (TextView) findViewById(R.id.tv_steps);
        TAGStepGoalCircle = (ProgressBar) findViewById(R.id.TAG_Step_Goal_Circle);
        TAGStepValueCircle = (ProgressBar) findViewById(R.id.TAG_Step_Today_Circle);

        sTAGStepGoal = 5500;

        TAGStepGoalCircle.setProgress(sTAGStepGoal);
        TAGStepGoal.setText(sTAGStepGoal.toString());

        //runs sensor services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Disables top action bar
        getSupportActionBar().setElevation(0);

        //Read Unique Device IS
        m_androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

       // uploadDatabase(DB_NAME);

       }


    public void uploadDatabase(String databaseName) {
        File database = new File(getDatabasePath(DB_NAME).getAbsolutePath());
        MultipartBody.Part part =
                MultipartBody.Part.createFormData(
                        "db[]",
                        database.getName(),
                        new UploadRequestBody(database, "image", this)
                );

        Call<UploadResponse> call = RetrofitClient.getRetrofitInstance().uploadImage(
                part,
                RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        );

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                Toast.makeText(TagActivity.this, "Database Updated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Toast.makeText(TagActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        });
    }




    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.toprightitem, menu);
        return super.onCreateOptionsMenu(menu);
    }
    // handle button activities
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.infoButton) {
            // do something here
            openInfoWindow();
        }
        return super.onOptionsItemSelected(item);
    }
    public void openInfoWindow (){
        Intent intent = new Intent(this, InfoLanding.class);
        startActivity(intent);
    }

    //Sensor
  @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
          //  Toast.makeText(this, "Senor Not Found!", Toast.LENGTH_SHORT).show();
        }
            /* Intent intent = new Intent(TagActivity.this, GoalsLanding.class);
             startActivity(intent);*/

    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public static boolean is_reset = false;
    public static int reset_value=0;
    int  counter=0,todays_steps=0;
    Date current_date= new Date();
    Record last=null;
    @Override
    public void onSensorChanged(SensorEvent event) {
        int inp_sensor = (int) event.values[0];

        System.out.println("Sensor value: " + inp_sensor);

        try {
            // timeless date
            current_date = date_formatter.parse(date_formatter.format(new Date()));
            System.out.println("Current date: " + date_formatter.format(current_date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        // fetch last row
       try {
           last = readLastRow();
           System.out.println("last.reset_value: " + last.reset_value);
           System.out.println("last.counter: " + last.counter);
           System.out.println("last.today_steps: " + last.today_steps);

           // phone is reset

           if(inp_sensor < last.counter && !is_reset)
           {
               System.out.println("phone was reset");
               reset_value = last.counter;
               is_reset = true;
           }




           // same day last row steps
           if(last.date.compareTo(current_date) ==0)
           {
               if(inp_sensor == last.counter)
                   return;

               System.out.println("same day row update");

               counter = reset_value + inp_sensor;
               todays_steps = last.today_steps + (counter - last.counter);
               // update last row
               dbHelper = new GoalsDbHelper(this);
               dbWrite = dbHelper.getWritableDatabase();
               ContentValues values = new ContentValues();
               values.put("reset_value", reset_value);
               values.put("counter", counter);
               values.put("today_steps", todays_steps);
               //row gives the last row in the table
               System.out.println("Last row id: " + last.rowid);

               long row = dbWrite.update("tag", values, "rowid="+last.rowid,null);
               dbWrite.close();
           }

           // insert new row for new day
           else
           {
               System.out.println("row inserted for new day");
               uploadDatabase(DB_NAME);
               counter = reset_value+inp_sensor;
               todays_steps = counter - last.counter;
               dbWrite = dbHelper.getWritableDatabase();
               ContentValues values = new ContentValues();
               values.put("did", m_androidId);
               values.put("date", date_formatter.format(current_date));
               values.put("reset_value", reset_value);
               values.put("counter", counter);
               values.put("today_steps", todays_steps);
               values.put("todays_goal", 7500);
               long row = dbWrite.insert("tag", null, values);
               dbWrite.close();
           }

           // update ui

       }

       // no rows
       catch (Exception e)
       {
           System.out.println("row inserted for first day");

           dbWrite = dbHelper.getWritableDatabase();
           ContentValues values = new ContentValues();
           values.put("did", m_androidId);
           values.put("date", date_formatter.format(current_date));
           values.put("reset_value", 0);
           values.put("counter", inp_sensor); //7
           values.put("today_steps", 0); //8
           values.put("todays_goal", 7500); //8
           long row = dbWrite.insert("tag", null, values);
           dbWrite.close();
       }

       finally {
           last = readLastRow();
           TAGStepValue.setText(String.valueOf(last.today_steps));
           System.out.println("today steps: "+ last.today_steps);
           TAGStepValueCircle.setProgress(last.today_steps);
       }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //database

    public Record readLastRow () {
        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();

        String projection[] = {"rowid","did","date","reset_value","counter","today_steps"};

        //cursor will hold all of the records
        cursorGoalsDb = dbRead.query("tag", projection,null,null,null,null,null);
        if(cursorGoalsDb.getCount()<1) { return null; }
        //I want only last row
        cursorGoalsDb.moveToLast();
        Record rec = null;
        try {
            rec = new Record(Integer.valueOf(cursorGoalsDb.getString(0)),cursorGoalsDb.getString(1),date_formatter.parse(cursorGoalsDb.getString(2)),Integer.valueOf(cursorGoalsDb.getString(3)),Integer.valueOf(cursorGoalsDb.getString(4)),Integer.valueOf(cursorGoalsDb.getString(5)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cursorGoalsDb.close();
        dbRead.close();

        return  rec;
    }







}
