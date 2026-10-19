package com.shop.common.events;

import java.math.BigDecimal;

/** Saga end state: the order is confirmed. */
public record OrderConfirmedEvent(String orderId, String customerEmail, BigDecimal totalAmount) {
}
