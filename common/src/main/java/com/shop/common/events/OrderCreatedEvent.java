package com.shop.common.events;

import java.math.BigDecimal;
import java.util.List;

/** Saga step 1: order-service created a PENDING order. */
public record OrderCreatedEvent(String orderId, String customerEmail, List<OrderItem> items, BigDecimal totalAmount) {
}
