package com.grishberg.data.model;

/**
 * Created by g on 13.08.15.
 */
public class MqOutMessage {
    private String clientQueueName;
    private String message;

    public MqOutMessage(String clientQueueName, String message) {
        this.clientQueueName = clientQueueName;
        this.message = message;
    }

    public String getClientQueueName() {
        return clientQueueName;
    }

    public String getMessage() {
        return message;
    }
}
