package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OagActivity extends AppCompatActivity implements SensorEventListener, UploadRequestBody.UploadCallback{
    public static final String DB_NAME = "PersuasiveTSO.db";
    TextView OAGStepToday, OAGStepOthers;
    ProgressBar OAGStepTodayCircle, OAGStepOthersCircle;
    String sOAGStepGoal;

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
    SQLiteDatabase SQLDataBase;
    public static boolean is_reset = false;
    public static int reset_value=0;
    int  counter=0,todays_steps=0;
    Date current_date= new Date();
    Record last=null;

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
        public int todays_goal;
        Record(int r,String dev_id,Date d,int a,int b, int c,int g) {
            rowid=r; did=dev_id; reset_value=a; counter=b; today_steps=c; date=d; todays_goal=g;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oag);
        getSupportActionBar().setElevation(0);

        OAGStepToday = (TextView) findViewById(R.id.OAG_Step_Today);
        OAGStepOthers = (TextView) findViewById(R.id.OAG_Step_Others);

        OAGStepTodayCircle = (ProgressBar) findViewById(R.id.OAG_Step_Today_Circle);
        OAGStepOthersCircle = (ProgressBar) findViewById(R.id.OAG_Step_Others_Circle);

      // sOAGStepGoal = "7500";
        dbHelper = new GoalsDbHelper(this);
        OtherStepAvgWeb content = new OtherStepAvgWeb();
      content.execute();






        //runs sensor services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Disables top action bar
        getSupportActionBar().setElevation(0);

        //Read Unique Device IS
        m_androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
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
                Toast.makeText(OagActivity.this, "Database Updated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Toast.makeText(OagActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.toprightitem, menu);
        return super.onCreateOptionsMenu(menu);




    }
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

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Senor Not Found!", Toast.LENGTH_SHORT).show();

        }
       /*Intent intent = new Intent(OagActivity.this, GoalsLanding.class);
        startActivity(intent);*/
    }
@Override
    protected void onPause() {
    super.onPause();
    finish();
}


    private class OtherStepAvgWeb extends AsyncTask<Void,Void,Void> {
        String OtherStepAvgWebValue;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            OAGStepOthersCircle.setProgress(Integer.parseInt(sOAGStepGoal));
            OAGStepOthers.setText(sOAGStepGoal);
           /* Toast toast = Toast.makeText(getApplicationContext(),
                    "Goal Value Updated",
                    Toast.LENGTH_SHORT);*/

           // toast.show();

            // Toast.makeText(sOAGStepGoal, "Goal Value Updated", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean got_server_value=false;

            try {
                String url = "https://atiq.eu/goals/";
                Document doc = Jsoup.connect(url).get();
                Elements data =  doc.select("span.OAGvalue");


                OtherStepAvgWebValue = data.select("span.OAGvalue").text();

                sOAGStepGoal = OtherStepAvgWebValue;
                got_server_value=true;
                System.out.println("Web Value is" + sOAGStepGoal );

            }
            catch (Exception e) {
                got_server_value=false;
                sOAGStepGoal = "7500";
                System.out.println("Goal Value is" + sOAGStepGoal );
                e.printStackTrace();
            }

            finally {

                try {
                    current_date = date_formatter.parse(date_formatter.format(new Date()));System.out.println("Current date: " + date_formatter.format(current_date));
                } catch (Exception e) { e.printStackTrace(); }

                // fetch last row
                last = readLastRow();

                // today already exists
                if(last!= null && last.date.compareTo(current_date) == 0)
                {

                    Record sec_last= read2ndLastRow();

                    // update with server value
                       if(got_server_value)
                       {
                           dbWrite = dbHelper.getWritableDatabase();
                           ContentValues values = new ContentValues();
                           values.put("todays_goal", Integer.parseInt(sOAGStepGoal));

                           long row = dbWrite.update("oag", values, "rowid="+last.rowid,null);
                           dbWrite.close();
                       }

                       // update with second last row
                       else if (sec_last != null)
                       {
                           dbWrite = dbHelper.getWritableDatabase();
                           ContentValues values = new ContentValues();
                           values.put("todays_goal", sec_last.todays_goal);
                           long row = dbWrite.update("oag", values, "rowid="+last.rowid,null);
                           dbWrite.close();
                       }

                       // update with 7500 value
                       else {
                           dbWrite = dbHelper.getWritableDatabase();
                           ContentValues values = new ContentValues();
                           values.put("todays_goal", 7500);
                           long row = dbWrite.update("oag", values, "rowid="+last.rowid,null);
                           dbWrite.close();
                       }

                }

                // today doesnot exist
                else {
                    System.out.println("row inserted for new day with 0 values");
                    dbWrite = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("did", m_androidId);
                    values.put("date", date_formatter.format(current_date));
                    values.put("reset_value", 0);
                    values.put("counter", 0);
                    values.put("today_steps", 0);
                    values.put("todays_goal", got_server_value? Integer.parseInt(sOAGStepGoal) : 7500);
                    long row = dbWrite.insert("oag", null, values);
                    dbWrite.close();
                }


                // fetch last row and update ui.

                try {
                    Record r = readLastRow();
                    sOAGStepGoal = String.valueOf(r.todays_goal);
                }
                catch (Exception e)
                {
                    sOAGStepGoal = String.valueOf("7500");
                }


            }




            return null;
        }
    }


    public OagActivity.Record readLastRow () {
        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();

        String projection[] = {"rowid","did","date","reset_value","counter","today_steps,todays_goal"};

        //cursor will hold all of the records
        cursorGoalsDb = dbRead.query("oag", projection,null,null,null,null,null);

        if(cursorGoalsDb.getCount()<1) { return null; }
        //I want only last row
        cursorGoalsDb.moveToLast();
        OagActivity.Record rec = null;
        try {
            rec = new OagActivity.Record(Integer.valueOf(cursorGoalsDb.getString(0)),cursorGoalsDb.getString(1),date_formatter.parse(cursorGoalsDb.getString(2)),Integer.valueOf(cursorGoalsDb.getString(3)),Integer.valueOf(cursorGoalsDb.getString(4)),Integer.valueOf(cursorGoalsDb.getString(5)),Integer.valueOf(cursorGoalsDb.getString(6)));
        } catch (Exception e) {
            System.out.println("db exception while fetching last row");
            e.printStackTrace();
        }

        cursorGoalsDb.close();
        dbRead.close();

        return  rec;
    }

    private Record read2ndLastRow() {

        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();

        String projection[] = {"rowid","did","date","reset_value","counter","today_steps","todays_goal"};

        OagActivity.Record rec = null;
        try {

            //cursor will hold all of the records
            cursorGoalsDb = dbRead.query("oag", projection,null,null,null,null,null);
            //I want second last row
            if(cursorGoalsDb.getCount() < 2)
            { return null; }
            cursorGoalsDb.moveToLast();
            cursorGoalsDb.moveToPrevious();
                rec = new OagActivity.Record(Integer.valueOf(cursorGoalsDb.getString(0)), cursorGoalsDb.getString(1), date_formatter.parse(cursorGoalsDb.getString(2)), Integer.valueOf(cursorGoalsDb.getString(3)), Integer.valueOf(cursorGoalsDb.getString(4)), Integer.valueOf(cursorGoalsDb.getString(5)), Integer.valueOf(cursorGoalsDb.getString(6)));
        }
        catch (Exception e) {
            System.out.println("db exception while fetching 2nd last row");
            e.printStackTrace();
        }

        finally {
            cursorGoalsDb.close();
            dbRead.close();
            return rec;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int inp_sensor = (int) event.values[0];

        System.out.println("Sensor value: " + inp_sensor);

        try {
            // timeless date
            current_date = date_formatter.parse(date_formatter.format(new Date()));
            System.out.println("Current date: " + date_formatter.format(current_date));

        } catch (Exception e) {
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

                long row = dbWrite.update("oag", values, "rowid="+last.rowid,null);
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
                long row = dbWrite.insert("oag", null, values);
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
            long row = dbWrite.insert("oag", null, values);
            dbWrite.close();
        }

        finally {
            last = readLastRow();
            OAGStepToday.setText(String.valueOf(last.today_steps));
            System.out.println("today steps: "+ last.today_steps);
            OAGStepTodayCircle.setProgress(Integer.valueOf((int) last.today_steps));
        }


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //upload DB to Server


}
