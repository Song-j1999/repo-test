package com.yy.rabbit.producer.broker;

import java.util.List;

import com.google.common.collect.Lists;
import com.yy.rabbit.api.Message;

public class MessageHolder {

    private List<Message> messages = Lists.newArrayList();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final ThreadLocal<MessageHolder> holder = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new MessageHolder();
        }
    };

    public static void add(Message message) {
        holder.get().messages.add(message);
    }

    public static List<Message> clear() {
        List<Message> tmp = Lists.newArrayList(holder.get().messages);
        holder.remove();
        return tmp;
    }

}
