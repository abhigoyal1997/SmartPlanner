package com.example.abhinav.smartplanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by abhi on 11/4/18.
 */

public class ClassEvent {
    private String courseCode;
    private boolean recur;
    private JSONArray days;
    private String from;
    private String to;

    public ClassEvent(JSONObject params) {
        try {
            this.courseCode = params.getString("courseCode");
            this.recur = params.getBoolean("recur");
            this.days = params.getJSONArray("days");
            this.from = params.getString("from");
            this.to = params.getString("to");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCourseCode() {
        return courseCode;
    }

    public boolean isRecur() {
        return recur;
    }

    public JSONArray getDays() {
        return days;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
