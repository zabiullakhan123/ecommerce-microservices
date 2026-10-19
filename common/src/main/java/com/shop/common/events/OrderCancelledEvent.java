package com.shop.common.events;


/** Saga end state: the order is cancelled. */
public record OrderCancelledEvent(String orderId, String customerEmail, String reason) {
}
