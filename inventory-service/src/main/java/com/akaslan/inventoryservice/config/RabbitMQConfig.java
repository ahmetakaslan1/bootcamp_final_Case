package com.akaslan.inventoryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INVENTORY_DEDUCTED_QUEUE = "inventory.deducted.queue";
    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String INVENTORY_DEDUCTED_ROUTING_KEY = "inventory.deducted.routing.key";

    @Bean
    public Queue inventoryDeductedQueue() {
        return new Queue(INVENTORY_DEDUCTED_QUEUE, true);
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Binding bindingInventoryDeducted() {
        return BindingBuilder.bind(inventoryDeductedQueue()).to(sagaExchange()).with(INVENTORY_DEDUCTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
