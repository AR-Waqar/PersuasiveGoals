package com.example.goals;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class DBSingleton {
    private static DBSingleton dbSingleton;
    private RequestQueue requestQueue;
    private static Context mContext;

    private DBSingleton (Context context)
    {
        mContext = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized DBSingleton  getInstance(Context context)
    {
        if (dbSingleton == null)
        {
            dbSingleton = new DBSingleton(context);
        }
        return  dbSingleton;
    }

    public RequestQueue getRequestQueue ()
    {
        if (requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return requestQueue;
    }

    public <T>void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }

}
