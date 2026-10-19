package com.shop.payment;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import com.shop.common.events.InventoryReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simulated payment gateway. Rules (configurable):
 * amounts above payment.max-amount are declined, and payment.failure-rate adds random declines.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repository;
    private final BigDecimal maxAmount;
    private final double failureRate;

    public PaymentService(PaymentRepository repository,
                          @Value("${payment.max-amount:150000}") BigDecimal maxAmount,
                          @Value("${payment.failure-rate:0.0}") double failureRate) {
        this.repository = repository;
        this.maxAmount = maxAmount;
        this.failureRate = failureRate;
    }

    @Transactional
    public Payment process(InventoryReservedEvent event) {
        return repository.findByOrderId(event.orderId())
                .map(existing -> {
                    log.info("Order {} already processed ({}); returning previous result",
                            event.orderId(), existing.getStatus());
                    return existing;
                })
                .orElseGet(() -> repository.save(charge(event)));
    }

    private Payment charge(InventoryReservedEvent event) {
        if (event.totalAmount().compareTo(maxAmount) > 0) {
            log.warn("Declined order {}: {} exceeds limit {}", event.orderId(), event.totalAmount(), maxAmount);
            return Payment.failed(event.orderId(), event.totalAmount(),
                    "Amount " + event.totalAmount() + " exceeds the limit of " + maxAmount);
        }
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            return Payment.failed(event.orderId(), event.totalAmount(), "Card declined by issuer");
        }
        log.info("Charged {} for order {}", event.totalAmount(), event.orderId());
        return Payment.success(event.orderId(), event.totalAmount());
    }
}
