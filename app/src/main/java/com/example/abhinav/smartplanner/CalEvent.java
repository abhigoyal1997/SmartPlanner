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

public class CalEvent {
    public static final int EVENT_CLASS = 1;
    public static final int EVENT_OTHER = 2;
    public static final String[] DAY = new String[]{"Su", "M", "Tu", "W", "Th", "F", "Sa"};

    public String name;
    public int type;
    public String courseCode;
    public boolean recur;
    public List<Integer> days;
    public long date;
    public long from;
    public long to;

    public CalEvent(){}

    public CalEvent(String name, int type, String courseCode, boolean recur, List<Integer> days, long date, long from, long to) {
        this.name = name;
        this.type = type;
        this.courseCode = courseCode;
        this.recur = recur;
        this.days = days;
        this.date = date;
        this.from = from;
        this.to = to;
    }

    public CalEvent(JSONObject params) {
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
            this.name = params.getString("name");
            if (params.has("courseCode")) {
                this.courseCode = params.getString("courseCode");
                this.type = EVENT_CLASS;
            } else {
                this.courseCode = null;
                this.type = EVENT_OTHER;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
