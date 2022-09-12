package com.yy.lockdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yy.distributedemo.dao")
public class DistributeDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributeDemoApplication.class, args);
    }

}
