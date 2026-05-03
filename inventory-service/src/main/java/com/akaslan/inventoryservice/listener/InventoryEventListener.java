package com.akaslan.inventoryservice.listener;

import com.akaslan.common.event.InventoryDeductedEvent;
import com.akaslan.common.event.OrderCreatedEvent;
import com.akaslan.common.event.OrderItemEventDto;
import com.akaslan.inventoryservice.config.RabbitMQConfig;
import com.akaslan.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            // Sepetteki tüm ürünlerin stoğunu düşüyoruz
            for (OrderItemEventDto item : event.items()) {
                inventoryService.deductInventory(item.productId(), item.quantity());
            }

            // Stok düşümü başarılıysa, Payment Service'in dinleyeceği olayı fırlatıyoruz
            InventoryDeductedEvent deductedEvent = new InventoryDeductedEvent(
                event.orderId(),
                event.customerId(),
                event.totalPrice(),
                event.paymentInfo()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.INVENTORY_DEDUCTED_ROUTING_KEY, deductedEvent);

        } catch (Exception e) {
            System.err.println("Stok düşülemedi (Sipariş ID: " + event.orderId() + "): " + e.getMessage());
            // TODO: İleride OrderCancelledEvent fırlatılıp sipariş iptal edilebilir.
        }
    }
}
