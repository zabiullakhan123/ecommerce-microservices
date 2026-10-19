package com.shop.order.domain;

public enum OrderStatus {
    /** Created; waiting for inventory and payment. */
    PENDING,
    /** Stock reserved and payment captured. */
    CONFIRMED,
    /** Rejected by inventory or payment. */
    CANCELLED
}
