package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


public class sagForm extends AppCompatActivity {
    private WebView SagForm;
    Button BtnRefresh;
    String URL = "http://irlstudies.dfki.de/index.php/survey/index/sid/255345/newtest/Y/lang/en";
    GoalsDbHelper dbHelper;
    SQLiteDatabase dbWrite;
    SQLiteDatabase dbRead;
    Cursor cursorScreensDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sag_form);

        BtnRefresh = (Button) findViewById(R.id.btnRefresher);
         BtnRefresh.setVisibility(View.GONE);

        SagForm = (WebView) findViewById(R.id.sagForm);
        SagForm.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
        System.out.println("webview loaded");
                super.onPageFinished(view, url);
                if(url.contains("index.php/255345")) {
                    updateStatus();
                }
            }
        });


        SagForm.getSettings().setJavaScriptEnabled(true);

        if (!DetectConnection.checkInternetConnection(this)) {
            Toast.makeText(getApplicationContext(), "No Internet, Please connect to working internet connection to proceed!", Toast.LENGTH_SHORT).show();
            BtnRefresh.setVisibility(View.VISIBLE);
            BtnRefresh.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    SagForm.loadUrl(URL);
                   String status = SagForm.getTitle();
                   if (SagForm.getTitle().equals("Webpage not available") || SagForm.getTitle().isEmpty()  )
                   {
                      //Toast.makeText(getApplicationContext(), "x x x !" + status, Toast.LENGTH_SHORT).show();
                       Toast.makeText(getApplicationContext(), "No Internet, Please connect to working internet connection to proceed!", Toast.LENGTH_SHORT).show();
                       BtnRefresh.setVisibility(View.VISIBLE);
                   }
                   else {
                       BtnRefresh.setVisibility(View.GONE);
                       Toast.makeText(getApplicationContext(), "Internet Available" , Toast.LENGTH_SHORT).show();
                   }
                }

            });
        }
        else {
            SagForm.loadUrl(URL);
        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Intent intent = new Intent(this, GoalsLanding.class);
//        startActivity(intent);
//    }





    public void updateStatus()
    {
        dbHelper = new GoalsDbHelper(this);
        dbWrite = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "completed");
        int last_id = readLastRowId();
        if (last_id != -1) {
            long row = dbWrite.update("screens", values, "rowid=" + last_id, null);
        }
        dbWrite.close();
    }

    public int readLastRowId () {
        dbHelper = new GoalsDbHelper(this);
        dbRead = dbHelper.getReadableDatabase();

        String projection[] = {"rowid","did","screen","start_date","end_date","status"};
        int rowid = -1;
        //cursor will hold all of the records

        try {
            cursorScreensDb = dbRead.query("screens", projection,null,null,null,null,null);
            //I want only last row
            cursorScreensDb.moveToLast();
            if(cursorScreensDb.getCount()!=0)
                rowid=Integer.valueOf(cursorScreensDb.getString(0));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        cursorScreensDb.close();
        dbRead.close();

        return rowid;
    }



}
