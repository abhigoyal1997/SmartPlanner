package com.example.abhinav.smartplanner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by abhi on 4/3/18.
 */

public class VolleyHandler {
    private static final VolleyHandler mInstance = new VolleyHandler();
    private RequestQueue mRQueue;

    public static VolleyHandler getInstance() {
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRQueue == null) {
            mRQueue = Volley.newRequestQueue(App.get().getApplicationContext());
        }
        return mRQueue;
    }

    private VolleyHandler() {
        mRQueue = getRequestQueue();
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
