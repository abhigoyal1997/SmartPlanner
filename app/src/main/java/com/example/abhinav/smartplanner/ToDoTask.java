package com.example.abhinav.smartplanner;

/**
 * Created by shraddheya on 11/4/18.
 */

class ToDoTask {
    String title;
    String date;
    String time;

    public ToDoTask(String title, String date, String time){
        this.time = time;
        this.date = date;
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
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
