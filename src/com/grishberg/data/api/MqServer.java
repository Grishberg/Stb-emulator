package com.grishberg.data.api;

import com.grishberg.data.model.MqOutMessage;
import com.rabbitmq.client.*;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by g on 13.08.15.
 */
public class MqServer {
    private static final String TAG = MqServer.class.getSimpleName();
    private static final int STATUS_RECEIVED_MESSAGE = 1;
    private static final int STATUS_SUBSCRIBED = 2;
    private static final int STATUS_PUBLISHED = 3;
    private static final int STATUS_QUEUE_NOT_EXISTS = 4;

    private String id;
    private String host;
    private String mMac;
    private String mExchange = "rpc";
    private ConnectionFactory mConnectionFactory;
    private Thread mPublishThread;
    private Thread mSubscribeThread;
    private BlockingDeque mOutMessagesQueue;

    private List<String> mDevicesQueues;
    private IMqObserver mMqObserver;

    public MqServer(String id, String host, String mac, IMqObserver observer) {
        this.id = id;
        this.host = host;
        this.mMac = mac;
        mMqObserver = observer;
        mDevicesQueues = new ArrayList<>(10);
        mConnectionFactory = new ConnectionFactory();
        mOutMessagesQueue = new LinkedBlockingDeque();
        setupConnectionFactory(host);
        initSubscribeToAMQP();
        initPublishToAMQP();
    }

    /**
     * open connection to MQ server
     *
     * @param uri
     */
    private void setupConnectionFactory(String uri) {
        try {
            mConnectionFactory.setAutomaticRecoveryEnabled(false);
            mConnectionFactory.setUri("amqp://" + uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMqMessage(MqOutMessage message) {
        try {
            mOutMessagesQueue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * start publishing thread
     */
    private void initPublishToAMQP() {
        mPublishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Connection connection = mConnectionFactory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.confirmSelect();
                        while (true) {
                            MqOutMessage mqOutMessage = (MqOutMessage) mOutMessagesQueue.takeFirst();
                            try {
                                String corrId = mqOutMessage.getCorrId();
                                String routingKey = mqOutMessage.getClientQueueName();
                                AMQP.BasicProperties props = new AMQP.BasicProperties
                                        .Builder()
                                        .correlationId(corrId)
                                                //.appId(mqOutMessage.getToken())
                                                //.replyTo(mqOutMessage.getClientQueueName())
                                        .build();
                                channel.basicPublish("", routingKey, props,
                                        mqOutMessage.getMessage().getBytes());
                                System.out.println("[s] " + mqOutMessage.getMessage());
                                channel.basicAck(mqOutMessage.getDeliveryTag(), false);
                                //channel.waitForConfirmsOrDie();
                            } catch (Exception e) {
                                System.out.println("[f] " + mqOutMessage.getMessage());
                                mOutMessagesQueue.putFirst(mqOutMessage);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.out.println("Connection broken: ");
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        mPublishThread.start();
    }

    /**
     * start subscription thread
     */
    private void initSubscribeToAMQP() {
        mSubscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Connection connection = null;
                    Channel channel = null;
                    try {
                        connection = mConnectionFactory.newConnection();
                        channel = connection.createChannel();
                        channel.exchangeDeclare(mExchange, "direct");

                        // создаем очередь для поступающих от МП сообщений
                        AMQP.Queue.DeclareOk serverQueue = channel.queueDeclare(mMac, false, false, true, null);

                        // забиндиться к очереди
                        AMQP.Queue.BindOk bindStatus = channel.queueBind(serverQueue.getQueue(), mExchange, mMac);

                        //sync consume
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(serverQueue.getQueue(), true, consumer);

                        // отправить сообщение об успешной операции
                        if (mMqObserver != null) {
                            mMqObserver.onBoundOk();
                        }


                        while (true) {
                            //ожидаем входящее сообщение
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());
                            //extract id and send to main thread
                            System.out.println("[r] " + message);
                            //TODO: разобрать рпц запрос
                            if (mMqObserver != null) {
                                String replyQueueName = delivery.getProperties().getReplyTo();
                                JSONRPC2Response response = mMqObserver.onMessage(replyQueueName, message);
                                if (response != null) {
                                    sendMqMessage(new MqOutMessage(
                                                    replyQueueName
                                                    , response.toJSONString()
                                                    , delivery.getProperties().getCorrelationId()
                                                    , delivery.getEnvelope().getDeliveryTag())
                                    );
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break;

                    } catch (Exception e1) {
                        System.out.println("Connection broken: ");
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        mSubscribeThread.start();
    }

    private void sendSimpleMessage(int status) {

    }

    public interface IMqObserver {
        void onBoundOk();

        JSONRPC2Response onMessage(String queueName, String msg);
    }

    public void release() {
        if (mSubscribeThread != null) {
            mSubscribeThread.interrupt();
        }
        if (mPublishThread != null) {
            mPublishThread.interrupt();
        }
        mSubscribeThread = null;
        mPublishThread = null;
    }
}
