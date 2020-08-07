package com.examples.chatapp.Model;

public class Chat
{
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String picture_URL;

    public Chat(String sender, String receiver, String message, boolean isseen,String picture_URL)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.picture_URL = picture_URL;
    }

    public Chat()
    {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen(){
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getPicture_URL() {
        return picture_URL;
    }

    public void setPicture_URL(String picture_URL) {
        this.picture_URL = picture_URL;
    }

}
