package com.example.goals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThankYouActivity extends AppCompatActivity implements UploadRequestBody.UploadCallback {

    public static final String DB_NAME = "PersuasiveTSO.db";

    Button SessionBooker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        uploadDatabase(DB_NAME);

        SessionBooker = (Button) findViewById(R.id.BtnSessionBook);

        SessionBooker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String url = "https://doodle.com/poll/vazbfsbhfrwmmi6p";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

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
                Toast.makeText(ThankYouActivity.this, "Database Updated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Toast.makeText(ThankYouActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    public void onProgressUpdate(int percentage) {

    }
}
