package com.yy.shardingjdbcdemo.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {
    private Long orderId;
    private BigDecimal orderAmount;
    private Integer orderStatus;
    private Integer userId;
}