package com.shop.common.events;


/** Saga step 2 (failure): not enough stock, so the order must be cancelled. */
public record InventoryRejectedEvent(String orderId, String reason) {
}
