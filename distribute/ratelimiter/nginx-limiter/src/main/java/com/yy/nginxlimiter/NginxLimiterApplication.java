package com.yy.nginxlimiter;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class NginxLimiterApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(NginxLimiterApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

}
