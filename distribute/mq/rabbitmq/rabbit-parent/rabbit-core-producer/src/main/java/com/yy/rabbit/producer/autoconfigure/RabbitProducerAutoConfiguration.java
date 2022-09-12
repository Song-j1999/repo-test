package com.yy.rabbit.producer.autoconfigure;

import com.yy.rabbit.task.annotation.EnableElasticJob;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * 	$RabbitProducerAutoConfiguration 自动装配 
 *
 */
@EnableElasticJob
@Configuration
@ComponentScan({"com.yy.rabbit.producer.*"})
public class RabbitProducerAutoConfiguration {


}
