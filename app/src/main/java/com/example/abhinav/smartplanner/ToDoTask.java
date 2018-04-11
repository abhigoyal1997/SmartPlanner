package com.example.abhinav.smartplanner;

/**
 * Created by shraddheya on 11/4/18.
 */

class ToDoTask {
    String title;
    long date;
    long time;

    public ToDoTask(String title, long date, long time){
        this.time = time;
        this.date = date;
        this.title = title;
    }

    public ToDoTask() {

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
