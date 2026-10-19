package com.shop.common.events;


/** Saga step 3 (failure): payment failed and triggers compensation (stock release). */
public record PaymentFailedEvent(String orderId, String reason) {
}
