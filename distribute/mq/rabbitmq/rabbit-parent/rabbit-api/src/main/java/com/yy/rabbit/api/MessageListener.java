package com.yy.rabbit.api;

/**
 * $MessageListener 消费者监听消息
 */
public interface MessageListener {

    void onMessage(Message message);

}
