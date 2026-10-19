package com.shop.common.events;

import java.math.BigDecimal;

/** Saga step 3 (success): payment was captured. */
public record PaymentCompletedEvent(String orderId, String paymentId, BigDecimal amount) {
}
