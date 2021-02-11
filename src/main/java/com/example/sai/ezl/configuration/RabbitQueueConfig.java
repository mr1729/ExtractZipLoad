package com.example.sai.ezl.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitQueueConfig {

    @Bean
    public Queue ezlRequestQueue() {
        return new Queue("ezlRequests", false);
    }

    @Bean
    public Queue ezlRepliesQueue() {
        return new Queue("ezlReplies", false);
    }
}