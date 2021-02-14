package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


public class tagForm extends AppCompatActivity {

    private WebView TagForm;
    Button BtnRefresh;
    String URL = "http://irlstudies.dfki.de/index.php/survey/index/sid/419616/newtest/Y/lang/en";
    GoalsDbHelper dbHelper;
    SQLiteDatabase dbWrite;
    SQLiteDatabase dbRead;
    Cursor cursorScreensDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_form);
        BtnRefresh = (Button) findViewById(R.id.btnRefresher);
        BtnRefresh.setVisibility(View.GONE);

        TagForm = (WebView) findViewById(R.id.tagForm);

        TagForm.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                System.out.println("webview loaded");
                super.onPageFinished(view, url);
                if(url.contains("index.php/419616")) {
                    updateStatus();
                }
             }
        });

        TagForm.getSettings().setJavaScriptEnabled(true);


        if (!DetectConnection.checkInternetConnection(this)) {
            Toast.makeText(getApplicationContext(), "No Internet, Please connect to working internet connection to proceed!", Toast.LENGTH_SHORT).show();
            BtnRefresh.setVisibility(View.VISIBLE);
            BtnRefresh.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    TagForm.loadUrl(URL);
                    String status = TagForm.getTitle();
                    if (TagForm.getTitle().equals("Webpage not available") || TagForm.getTitle().isEmpty()  )
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
                TagForm.loadUrl(URL);
        }


    }

//
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
