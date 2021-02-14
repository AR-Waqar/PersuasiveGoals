package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class InfoLanding extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_landing);
        getSupportActionBar().setElevation(0);
    }



    // handle button activities

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.closeButton) {
            // do something here
           // openInfoWindow();
        }
        return super.onOptionsItemSelected(item);
    }

   /* public void openInfoWindow (){

    }*/

}
