package com.sdsuchatapp.chatitout;

public class MessageBean {
    private String message;
    private boolean seen;
    private long time;
    private String from;
    private String type;

    public MessageBean() {
    }

    public MessageBean(String message, boolean seen, long time, String from, String type) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.from = from;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
