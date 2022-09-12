package com.yy.rabbit.common.serializer.impl;


import com.yy.rabbit.api.Message;
import com.yy.rabbit.common.serializer.Serializer;
import com.yy.rabbit.common.serializer.SerializerFactory;

public class JacksonSerializerFactory implements SerializerFactory {

    public static final SerializerFactory INSTANCE = new JacksonSerializerFactory();

    @Override
    public Serializer create() {
        return JacksonSerializer.createParametricType(Message.class);
    }

}
