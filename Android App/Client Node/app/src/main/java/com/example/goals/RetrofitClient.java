package com.example.goals;
import  retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static MyAPI getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl("https://atiq.eu/test/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI.class);
    }
}


