package com.shop.inventory;

import com.shop.common.events.InventoryRejectedEvent;
import com.shop.common.events.InventoryReservedEvent;
import com.shop.common.events.OrderCreatedEvent;
import com.shop.common.events.PaymentCompletedEvent;
import com.shop.common.events.PaymentFailedEvent;
import com.shop.common.kafka.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventListener(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = Topics.ORDER_CREATED)
    public void onOrderCreated(OrderCreatedEvent event) {
        // The DB transaction commits inside reserve(); we publish only afterwards.
        InventoryService.ReservationResult result = inventoryService.reserve(event);
        if (result.success()) {
            kafkaTemplate.send(Topics.INVENTORY_RESERVED, event.orderId(),
                    new InventoryReservedEvent(event.orderId(), event.customerEmail(), event.totalAmount())).join();
        } else {
            kafkaTemplate.send(Topics.INVENTORY_REJECTED, event.orderId(),
                    new InventoryRejectedEvent(event.orderId(), result.reason())).join();
        }
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED)
    public void onPaymentFailed(PaymentFailedEvent event) {
        inventoryService.release(event.orderId());
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        inventoryService.commit(event.orderId());
    }
}
