package com.grishberg.data.model;

/**
 * Created by g on 15.08.15.
 */
public class QueueInfo {
    private String name;
    private String correlationId;

    public QueueInfo(String name, String correlationId) {
        this.name = name;
        this.correlationId = correlationId;
    }

    public QueueInfo(String name) {
        this(name,null);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getName() {
        return name;
    }
}
