package com.shop.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Records which order holds which stock, so the saga can be compensated and replays are idempotent. */
@Entity
@Table(name = "reservations", indexes = @Index(name = "idx_reservation_order", columnList = "orderId"))
public class Reservation {

    public enum Status { RESERVED, COMMITTED, RELEASED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String skuCode;

    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected Reservation() {
    }

    public Reservation(String orderId, String skuCode, int quantity) {
        this.orderId = orderId;
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.status = Status.RESERVED;
    }

    public String getOrderId() { return orderId; }
    public String getSkuCode() { return skuCode; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
