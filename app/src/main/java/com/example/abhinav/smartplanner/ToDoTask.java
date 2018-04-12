package com.example.abhinav.smartplanner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shraddheya on 11/4/18.
 */

class ToDoTask {
    String title;

    public ToDoTask(String title, String id, long date, long time) {
        this.title = title;
        this.id = id;
        this.date = date;
        this.time = time;
    }

    public String id;
    long date;
    long time;

    public ToDoTask(String title, long date, long time){
        this.time = time;
        this.date = date;
        this.title = title;
    }

    public ToDoTask() {

    }

    public ToDoTask(JSONObject params) {
        try {
            this.date = params.getLong("date");
            this.time = params.getLong("time");
            this.title = params.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "title: " + title + "\n" +
                "date: " + date + "\n" +
                "time" + time + "\n";
    }
}
