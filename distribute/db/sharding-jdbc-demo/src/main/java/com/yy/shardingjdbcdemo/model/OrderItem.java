package com.yy.shardingjdbcdemo.model;


import lombok.Data;

@Data
public class OrderItem {

    private Integer id;


    private Integer orderId;


    private String pruductName;


    private Integer num;


    private Integer userId;


}