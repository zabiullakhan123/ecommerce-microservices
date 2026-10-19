package com.shop.common.events;

import java.math.BigDecimal;

/** Saga step 2 (success): stock is held for the order. */
public record InventoryReservedEvent(String orderId, String customerEmail, BigDecimal totalAmount) {
}
