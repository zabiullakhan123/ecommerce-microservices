package com.shop.payment;

import com.shop.common.events.InventoryReservedEvent;
import com.shop.common.events.PaymentCompletedEvent;
import com.shop.common.events.PaymentFailedEvent;
import com.shop.common.kafka.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventListener(PaymentService paymentService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = Topics.INVENTORY_RESERVED)
    public void onInventoryReserved(InventoryReservedEvent event) {
        Payment payment = paymentService.process(event);
        if (payment.getStatus() == Payment.Status.SUCCESS) {
            kafkaTemplate.send(Topics.PAYMENT_COMPLETED, event.orderId(),
                    new PaymentCompletedEvent(event.orderId(), payment.getId(), payment.getAmount())).join();
        } else {
            kafkaTemplate.send(Topics.PAYMENT_FAILED, event.orderId(),
                    new PaymentFailedEvent(event.orderId(), payment.getFailureReason())).join();
        }
    }
}
