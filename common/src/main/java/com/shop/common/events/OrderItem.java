package com.shop.common.events;

import java.math.BigDecimal;

/** A single line of an order, carried inside saga events. */
public record OrderItem(String skuCode, int quantity, BigDecimal unitPrice) {
}
