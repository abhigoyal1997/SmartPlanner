package com.example.abhinav.smartplanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by abhi on 11/4/18.
 */

public class Event {
    public static final int EVENT_CLASS = 1;
    public static final int EVENT_OTHER = 2;

    public String name;
    public int type;
    public String courseCode;
    public boolean recur;
    public List<Integer> days;
    public long date;
    public long from;
    public long to;

    public Event(JSONObject params, int type) {
        this.type = type;
        try {
            this.recur = params.getBoolean("recur");
            this.days = new ArrayList<>();
            JSONArray daysArray = params.getJSONArray("days");
            if (daysArray != null) {
                for (int i = 0; i < daysArray.length(); i++) {
                    this.days.add(daysArray.getInt(i));
                }
            }
            this.date = params.getLong("date");
            this.from = params.getLong("from");
            this.to = params.getLong("to");
            if (params.has("courseCode")) {
                this.courseCode = params.getString("courseCode");
            } else {
                this.courseCode = null;
            }
            if (type == EVENT_CLASS) {
                this.name = this.courseCode + " class";
            } else {
                this.name = params.getString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
