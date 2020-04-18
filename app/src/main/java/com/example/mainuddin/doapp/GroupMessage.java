package com.example.mainuddin.doapp;

public class GroupMessage {
    private String from;
    private String message;
    private String type;
    private String time;
    private String date;
    private String name;


    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    private String messageKey;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    private String groupName;


    public int getPollY() {
        return pollY;
    }

    public void setPollY(int pollY) {
        this.pollY = pollY;
    }

    public int getPollN() {
        return pollN;
    }

    public void setPollN(int pollN) {
        this.pollN = pollN;
    }

    private int pollY,pollN;



    public GroupMessage()
    {

    }

    public GroupMessage(String from, String message, String type , String time, String date, String name,int pollY,int pollN,String messageKey,String  groupName) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.date = date;
        this.name = name;
        this.pollY = pollY;
        this.pollN = pollN;
        this.messageKey = messageKey;
        this.groupName = groupName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
