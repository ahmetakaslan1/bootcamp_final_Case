package com.akaslan.paymentservice.listener;

import com.akaslan.common.event.InventoryDeductedEvent;
import com.akaslan.common.event.PaymentFailedEvent;
import com.akaslan.common.event.PaymentSuccessfulEvent;
import com.akaslan.paymentservice.config.RabbitMQConfig;
import com.akaslan.paymentservice.dto.PaymentRequest;
import com.akaslan.paymentservice.entity.Payment;
import com.akaslan.paymentservice.entity.PaymentStatus;
import com.akaslan.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "inventory.deducted.queue")
    public void handleInventoryDeducted(InventoryDeductedEvent event) {
        try {
     
            PaymentRequest request = new PaymentRequest(
                event.orderId(),
                event.totalPrice(),
                event.paymentInfo().cardNumber(),
                event.paymentInfo().expireMonth(),
                event.paymentInfo().expireYear(),
                event.paymentInfo().cvc(),
                event.paymentInfo().cardHolderName()
            );

            // Mevcut İyzico Service'i çağır
            Payment payment = paymentService.processPayment(request);

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                // Ödeme Başarılıysa, Order Service'e haber ver
                PaymentSuccessfulEvent successEvent = new PaymentSuccessfulEvent(event.orderId(), event.customerId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY, successEvent);
            } else {
                System.err.println("Ödeme başarısız oldu (Sipariş ID: " + event.orderId() + ")");
                PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    event.orderId(),
                    event.customerId(),
                    "Kart numarası geçersizdir"
                );
                rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, failedEvent);
            }

        } catch (Exception e) {
            System.err.println("Ödeme işlemi sırasında hata oluştu (Sipariş ID: " + event.orderId() + "): " + e.getMessage());
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                event.orderId(),
                event.customerId(),
                e.getMessage()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, failedEvent);
        }
    }
}
