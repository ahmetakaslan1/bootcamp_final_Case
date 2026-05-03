package com.akaslan.orderservice.listener;

import com.akaslan.common.event.PaymentFailedEvent;
import com.akaslan.common.event.PaymentSuccessfulEvent;
import com.akaslan.orderservice.client.CartClient;
import com.akaslan.orderservice.entity.Order;
import com.akaslan.orderservice.entity.OrderStatus;
import com.akaslan.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final int MAX_REASON_LENGTH = 450;
    private final OrderRepository orderRepository;
    private final CartClient cartClient;

    @RabbitListener(queues = "payment.success.queue")
    @Transactional
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        try {
            Order order = orderRepository.findByOrderNumber(event.orderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + event.orderId()));

            if (order.getStatus() == OrderStatus.COMPLETED) {
                return;
            }

            order.setStatus(OrderStatus.COMPLETED);
            order.setPaymentFailureReason(null);
            orderRepository.save(order);

          
            cartClient.clearCartByUserId(event.customerId());

            System.out.println("Sipariş Başarıyla Tamamlandı! Order ID: " + event.orderId());

        } catch (Exception e) {
            System.err.println("Sipariş tamamlanamadı (Order ID: " + event.orderId() + "): " + e.getMessage());
        }
    }

    @RabbitListener(queues = "payment.failed.queue")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        try {
            Order order = orderRepository.findByOrderNumber(event.orderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + event.orderId()));

            if (order.getStatus() == OrderStatus.COMPLETED) {
                return;
            }

            order.setStatus(OrderStatus.FAILED);
            String reason = event.reason() == null ? "Ödeme başarısız oldu." : event.reason();
            if (reason.length() > MAX_REASON_LENGTH) {
                reason = reason.substring(0, MAX_REASON_LENGTH);
            }
            order.setPaymentFailureReason(reason);
            orderRepository.save(order);

            System.err.println("Sipariş ödeme hatasıyla FAILED oldu. Order ID: " + event.orderId() + " | Sebep: "
                    + event.reason());
        } catch (Exception e) {
            System.err.println("PaymentFailedEvent işlenemedi (Order ID: " + event.orderId() + "): " + e.getMessage());
        }
    }
}
