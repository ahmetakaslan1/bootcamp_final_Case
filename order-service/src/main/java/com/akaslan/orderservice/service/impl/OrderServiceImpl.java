package com.akaslan.orderservice.service.impl;

import com.akaslan.common.event.OrderCreatedEvent;
import com.akaslan.common.event.OrderItemEventDto;
import com.akaslan.common.event.PaymentInfoEventDto;
import com.akaslan.orderservice.client.CartClient;
import com.akaslan.orderservice.client.CartItemResponse;
import com.akaslan.orderservice.client.CartResponse;
import com.akaslan.orderservice.config.RabbitMQConfig;
import com.akaslan.orderservice.dto.OrderRequest;
import com.akaslan.orderservice.dto.OrderResponse;
import com.akaslan.orderservice.entity.Order;
import com.akaslan.orderservice.entity.OrderItem;
import com.akaslan.orderservice.entity.OrderStatus;
import com.akaslan.orderservice.repository.OrderRepository;
import com.akaslan.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient; 
    private final ApplicationEventPublisher eventPublisher; 

    @Override
    @Transactional
    public OrderResponse createOrder(String customerId, OrderRequest request) {
        
        
        CartResponse cart = null;
        try {
            var response = cartClient.getMyCart();
            if (response != null && response.getData() != null) {
                cart = response.getData();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Sepet bilgileri alınamadı: " + e.getMessage());
        }

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Sepetiniz boş! Sipariş oluşturulamaz.");
        }

     
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setCustomerId(customerId);
        order.setTotalPrice(cart.totalPrice());
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(request.shippingAddress());
        order.setReceiverName(request.receiverName());
        order.setPhoneNumber(request.phoneNumber());

        List<OrderItem> orderItems = cart.items().stream()
            .map(ci -> OrderItem.builder()
                .productId(ci.productId())
                .quantity(ci.quantity())
                .price(ci.price())
                .order(order)
                .build())
            .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

     
        PaymentInfoEventDto paymentInfo = new PaymentInfoEventDto(
            request.cardNumber(),
            request.expireMonth(),
            request.expireYear(),
            request.cvc(),
            request.cardHolderName()
        );

        List<OrderItemEventDto> eventItems = cart.items().stream()
            .map(ci -> new OrderItemEventDto(ci.productId(), ci.quantity(), ci.price()))
            .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getOrderNumber(),
            customerId,
            savedOrder.getTotalPrice(),
            eventItems,
            paymentInfo
        );

        eventPublisher.publishEvent(event);

        return OrderResponse.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(OrderResponse::from)
            .toList();
    }
}
