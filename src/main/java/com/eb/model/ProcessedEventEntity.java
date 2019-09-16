package com.eb.model;

public class ProcessedEventEntity {

    public ProcessedEventEntity(String id, int duration, String type, String host, boolean alert) {
        this.id = id;
        this.duration = duration;
        this.type = type;
        this.host = host;
        this.alert = alert;
    }

    private final String id;
    private final int duration;
    private final String type;
    private final String host;
    private final boolean alert;

    public String getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public boolean isAlert() {
        return alert;
    }

}
