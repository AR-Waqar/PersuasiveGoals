package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Random;
import java.lang.Object;
import java.util.Set;

public class GoalsLanding extends AppCompatActivity {

Map<String, String> total_screens = new HashMap<String,String>();
    String m_androidId;
    public static final String date_format = "dd-MM-yyyy";
    public SimpleDateFormat date_formatter = new SimpleDateFormat(date_format);
    GoalsDbHelper dbHelper;
    SQLiteDatabase dbWrite;
    SQLiteDatabase dbRead;
    Cursor cursorScreensDb;

// helper classes
    public class Record {
        public int rowid;
        public String did;
        public String screen;
        public Date start_date;
        public Date end_date;
        public String status;
        Record(int r,String d,String s,Date st_d,Date end_d, String stat) {
            rowid=r; did=d; screen=s; start_date=st_d; end_date=end_d; status=stat;
        }
    }
    public static class DateUtil {
        public static Date addDays(Date date, int days)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, days); //minus number would decrement the days
            return cal.getTime();
        }
    }

    private Button TagButton, SagButton, OagButton, TagForm, SagForm, OagForm, WelcomeBtn, ThankYouBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        total_screens.put("OagActivity","oagForm");
        total_screens.put("SagActivity","sagForm");
        total_screens.put("TagActivity","tagForm");
        m_androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_landing);
        decideScreen();
    }

    @Override
    public void onResume(){
        super.onResume();
        decideScreen();
    }


    public void decideScreen() {
        // java bullshit with dates
        Record last = readLastRow();
        Date current_date = new Date();
        Date current_plus7 = new Date();

        try {
            // timeless date
            current_date = date_formatter.parse(date_formatter.format(new Date()));
            System.out.println("Current date: " + date_formatter.format(current_date));
            current_plus7 = DateUtil.addDays(current_date, 6);
        }
        catch (ParseException e) {
        e.printStackTrace();
        }

      // welcome activity
        if(last == null)
        {
                Record r = new Record(0,m_androidId,"WelcomeActivity",current_date,current_plus7,"running");
                insertRecord(r);
        }

        // welcome stage has passed
        else if(last.screen.contains("WelcomeActivity") && current_date.compareTo(last.end_date) > 0)
        {
            // update welcome screen status
            dbHelper = new GoalsDbHelper(this);
            dbWrite = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", "completed");
            long row = dbWrite.update("screens", values, "rowid="+last.rowid,null);
            dbWrite.close();

            // insert random key from total screens map
            String screen = (String) total_screens.keySet().toArray()[new Random().nextInt(3)];
            System.out.println("first task screen: " + screen);
            Record r = new Record(0,m_androidId,screen,current_date,current_plus7,"running");
            insertRecord(r);
        }

        // if its activity screen and past its due date
        else if(last.screen.contains("Activity") && current_date.compareTo(last.end_date) > 0)
        {
            // update activity screen status
            dbHelper = new GoalsDbHelper(this);
            dbWrite = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", "completed");
            long row = dbWrite.update("screens", values, "rowid="+last.rowid,null);
            dbWrite.close();

            // insert form value for that screen
            Record r = new Record(0,m_androidId,total_screens.get(last.screen),current_date,current_date,"running");
            insertRecord(r);
        }

        // if the form is submitted in its webview
        else if(last.screen.contains("Form") && last.status.contains("completed"))
        {
            Set<String> screens_completed = read_screens_from_db();
            Set<String> totaaal = new HashSet<>();
            for (Map.Entry<String,String> entry : total_screens.entrySet())
               totaaal.add(entry.getKey());
            totaaal.removeAll(screens_completed);
            // if all tasks completed show thankyou screen
            if(totaaal.size() == 0) {
                Record r = new Record(0,m_androidId,"ThankYouActivity",current_date,current_date,"finished");
                insertRecord(r);
            }
            else {
                Record r = new Record(0,m_androidId,totaaal.iterator().next(),current_date,current_plus7,"running");
                insertRecord(r);
            }
        }

        last = readLastRow();

renderActivity(last.screen);
    }

// helpers
    public Set<String> read_screens_from_db () {
        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();
        Set<String> temp = new HashSet<String>();

        String projection[] = {"rowid","did","screen","start_date","end_date","status"};

        //cursor will hold all of the records
        cursorScreensDb = dbRead.query("screens", projection,null,null,null,null,null);
        while (cursorScreensDb.moveToNext()) {
            temp.add(cursorScreensDb.getString(2));
        }

        cursorScreensDb.close();
        dbRead.close();

        return  temp;
    }
    public void insertRecord(Record rec) {
    dbWrite = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("did", m_androidId);
    values.put("screen", rec.screen);
    values.put("start_date", date_formatter.format(rec.start_date));
    values.put("end_date", date_formatter.format(rec.end_date));
    values.put("status", rec.status);
    long row = dbWrite.insert("screens", null, values);
    dbWrite.close();
}
    public Record readLastRow () {
        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();

        String projection[] = {"rowid","did","screen","start_date","end_date","status"};

        //cursor will hold all of the records

        Record rec = null;
        try {
            cursorScreensDb = dbRead.query("screens", projection,null,null,null,null,null);
            //I want only last row
            cursorScreensDb.moveToLast();
            if(cursorScreensDb.getCount()!=0)
            rec = new Record(Integer.valueOf(cursorScreensDb.getString(0)),cursorScreensDb.getString(1),cursorScreensDb.getString(2),date_formatter.parse(cursorScreensDb.getString(3)),date_formatter.parse(cursorScreensDb.getString(4)),cursorScreensDb.getString(5));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cursorScreensDb.close();
        dbRead.close();

        return  rec;
    }
    public void renderActivity(String str){
        System.out.println("current_activity: "+str);
        if(str.contains("OagActivity")) {        Intent intent = new Intent(this, OagActivity.class);startActivity(intent); }
        if(str.contains("TagActivity")) {        Intent intent = new Intent(this, TagActivity.class);startActivity(intent); }
        if(str.contains("SagActivity")) {        Intent intent = new Intent(this, SagActivity.class);startActivity(intent); }
        if(str.contains("oagForm")) {        Intent intent = new Intent(this, oagForm.class);startActivity(intent); }
        if(str.contains("sagForm")) {        Intent intent = new Intent(this, sagForm.class);startActivity(intent); }
        if(str.contains("tagForm")) {        Intent intent = new Intent(this, tagForm.class);startActivity(intent); }
        if(str.contains("WelcomeActivity")) { Intent intent = new Intent(this, WelcomeActivity.class);startActivity(intent); }
        if(str.contains("ThankYouActivity")) { Intent intent = new Intent(this, ThankYouActivity.class);startActivity(intent); }
    }

}
