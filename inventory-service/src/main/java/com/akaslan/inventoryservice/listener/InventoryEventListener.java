package com.akaslan.inventoryservice.listener;

import com.akaslan.common.event.InventoryDeductedEvent;
import com.akaslan.common.event.OrderCreatedEvent;
import com.akaslan.common.event.OrderFailedEvent;
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
          
            for (OrderItemEventDto item : event.items()) {
                inventoryService.deductInventory(item.productId(), item.quantity());
            }

        
            InventoryDeductedEvent deductedEvent = new InventoryDeductedEvent(
                event.orderId(),
                event.customerId(),
                event.totalPrice(),
                event.paymentInfo()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.INVENTORY_DEDUCTED_ROUTING_KEY, deductedEvent);

        } catch (Exception e) {
            System.err.println("Stok düşülemedi (Sipariş ID: " + event.orderId() + "): " + e.getMessage());

        }
    }

    @RabbitListener(queues = "order.failed.queue")
    public void handleOrderFailed(OrderFailedEvent event) {
        try {
            System.out.println("Ödeme başarısız veya Sipariş iptal, Stoklar geri yükleniyor... Sipariş ID: " + event.orderId());
            for (OrderItemEventDto item : event.items()) {
                inventoryService.restoreInventory(item.productId(), item.quantity());
            }
        } catch (Exception e) {
            System.err.println("Stok iadesi yapılamadı (Sipariş ID: " + event.orderId() + "): " + e.getMessage());
        }
    }
}
