package com.shop.common.kafka;

import java.util.List;

/** Kafka topic names used by the order saga. Messages are keyed by orderId. */
public final class Topics {

    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_REJECTED = "inventory.rejected";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_CANCELLED = "order.cancelled";

    public static final List<String> ALL = List.of(
            ORDER_CREATED, INVENTORY_RESERVED, INVENTORY_REJECTED,
            PAYMENT_COMPLETED, PAYMENT_FAILED, ORDER_CONFIRMED, ORDER_CANCELLED);

    private Topics() {
    }
}
