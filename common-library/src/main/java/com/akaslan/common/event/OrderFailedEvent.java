package com.akaslan.common.event;

import java.util.List;

public record OrderFailedEvent(
    String orderId,
    List<OrderItemEventDto> items,
    String reason
) {}
