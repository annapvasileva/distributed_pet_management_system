package ru.annapvasileva.configurations;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CAT_EXCHANGE = "cat.exchange";
    public static final String OWNER_EXCHANGE = "owner.exchange";

    @Bean
    public TopicExchange catExchange() {
        return new TopicExchange(CAT_EXCHANGE);
    }

    @Bean
    public TopicExchange ownerExchange() {
        return new TopicExchange(OWNER_EXCHANGE);
    }

    private Queue queue(String name) {
        return new Queue(name, true, false, false);
    }

    @Bean
    public Queue catDeleteByOwnerQueue() {
        return queue("cat.deleteByOwner.queue");
    }

    @Bean
    public Queue catDeleteByOwnerReplyQueue() {
        return queue("cat.deleteByOwner.reply.queue");
    }

    private Binding binding(Queue queue, TopicExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding catDeleteByOwnerBinding() {
        return binding(catDeleteByOwnerQueue(), catExchange(), "cat.deleteByOwner");
    }

    @Bean
    public Binding catDeleteByOwnerReplyBinding() {
        return binding(catDeleteByOwnerReplyQueue(), catExchange(), "cat.deleteByOwner.reply");
    }
}

