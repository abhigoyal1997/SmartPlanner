package com.example.abhinav.smartplanner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abhi on 10/4/18.
 */

public interface OnResponseListener {
    void onResponse(JSONObject response) throws JSONException;
}