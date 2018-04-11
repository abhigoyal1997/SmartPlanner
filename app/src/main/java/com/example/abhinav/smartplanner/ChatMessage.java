package com.example.abhinav.smartplanner;

import java.util.Date;

/**
 * Created by abhi on 7/4/18.
 */

public class ChatMessage {

    private String text;
    private String sender;
    private Date timestamp;

    public ChatMessage(String text, String sender, Date timestamp){
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public ChatMessage(){

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}