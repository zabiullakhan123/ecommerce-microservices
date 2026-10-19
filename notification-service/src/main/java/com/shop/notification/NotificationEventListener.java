package com.shop.notification;

import com.shop.common.events.OrderCancelledEvent;
import com.shop.common.events.OrderConfirmedEvent;
import com.shop.common.kafka.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = Topics.ORDER_CONFIRMED)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        notificationService.send(event.orderId(), event.customerEmail(),
                "Order confirmed",
                "Your order " + event.orderId() + " is confirmed. Amount charged: INR " + event.totalAmount());
    }

    @KafkaListener(topics = Topics.ORDER_CANCELLED)
    public void onOrderCancelled(OrderCancelledEvent event) {
        notificationService.send(event.orderId(), event.customerEmail(),
                "Order cancelled",
                "Sorry, your order " + event.orderId() + " was cancelled. Reason: " + event.reason());
    }
}
