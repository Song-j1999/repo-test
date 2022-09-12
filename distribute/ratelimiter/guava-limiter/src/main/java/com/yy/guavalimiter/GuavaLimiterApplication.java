package com.yy.guavalimiter;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class GuavaLimiterApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(GuavaLimiterApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

}
