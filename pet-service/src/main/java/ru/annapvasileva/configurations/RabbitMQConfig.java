package ru.annapvasileva.configurations;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CAT_EXCHANGE = "cat.exchange";

    @Bean
    public TopicExchange catExchange() {
        return new TopicExchange(CAT_EXCHANGE);
    }
}

