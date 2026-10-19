package com.shop.order.saga;

import com.shop.common.events.InventoryRejectedEvent;
import com.shop.common.events.PaymentCompletedEvent;
import com.shop.common.events.PaymentFailedEvent;
import com.shop.common.kafka.Topics;
import com.shop.order.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Reacts to the other participants' outcomes and moves the order to its final state. */
@Component
public class OrderSagaListener {

    private final OrderService orderService;

    public OrderSagaListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = Topics.INVENTORY_REJECTED)
    public void onInventoryRejected(InventoryRejectedEvent event) {
        orderService.cancel(event.orderId(), event.reason());
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        orderService.confirm(event.orderId());
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED)
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderService.cancel(event.orderId(), "Payment failed: " + event.reason());
    }
}
