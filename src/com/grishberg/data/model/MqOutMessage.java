package com.grishberg.data.model;

/**
 * Created by g on 13.08.15.
 */
public class MqOutMessage {
    private String clientQueueName;
    private String message;
    private String corrId;
    private long deliveryTag;

    public MqOutMessage(String clientQueueName, String message) {
        this.clientQueueName = clientQueueName;
        this.message = message;
    }

    public MqOutMessage(String clientQueueName, String message, String corrId) {
        this.clientQueueName = clientQueueName;
        this.message = message;
        this.corrId = corrId;
    }

    public String getCorrId() {
        return corrId;
    }

    public String getClientQueueName() {
        return clientQueueName;
    }

    public String getMessage() {
        return message;
    }
}
