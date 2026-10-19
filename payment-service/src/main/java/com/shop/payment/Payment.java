package com.shop.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    public enum Status { SUCCESS, FAILED }

    @Id
    private String id;

    /** Unique: guarantees at most one charge per order, even if the event is redelivered. */
    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String failureReason;

    private Instant createdAt;

    protected Payment() {
    }

    private Payment(String orderId, BigDecimal amount, Status status, String failureReason) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = Instant.now();
    }

    public static Payment success(String orderId, BigDecimal amount) {
        return new Payment(orderId, amount, Status.SUCCESS, null);
    }

    public static Payment failed(String orderId, BigDecimal amount, String reason) {
        return new Payment(orderId, amount, Status.FAILED, reason);
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public Status getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
