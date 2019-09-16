package com.eb.model;

import java.util.StringJoiner;

public class LoggerEvent {
    public LoggerEvent(String id, State state, String type, String host, Long timestamp) {
        this.id = id;
        this.state = state;
        this.type = type;
        this.host = host;
        this.timestamp = timestamp;
    }

    private final String id;
    private final State state;
    private final String type;
    private final String host;
    private final Long timestamp;

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LoggerEvent.class.getSimpleName() + "[", "]").add("id='" + id + "'")
                .add("state=" + state)
                .add("type='" + type + "'")
                .add("host='" + host + "'")
                .add("timestamp=" + timestamp)
                .toString();
    }
}
