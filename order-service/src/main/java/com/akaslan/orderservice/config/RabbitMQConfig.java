package com.akaslan.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created.routing.key";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success.routing.key";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed.routing.key";

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(PAYMENT_SUCCESS_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Binding bindingOrderCreated() {
        return BindingBuilder.bind(orderCreatedQueue()).to(sagaExchange()).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentSuccess() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(sagaExchange()).with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentFailed() {
        return BindingBuilder.bind(paymentFailedQueue()).to(sagaExchange()).with(PAYMENT_FAILED_ROUTING_KEY);
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
