package com.shop.order;

import java.util.List;

import com.shop.common.events.OrderCancelledEvent;
import com.shop.common.events.OrderConfirmedEvent;
import com.shop.common.events.OrderCreatedEvent;
import com.shop.common.events.OrderItem;
import com.shop.common.kafka.Topics;
import com.shop.order.client.ProductClient;
import com.shop.order.client.ProductClient.ProductDto;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderLine;
import com.shop.order.domain.OrderRepository;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.web.OrderDtos.CreateOrderRequest;
import com.shop.order.web.OrderDtos.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionTemplate tx;

    public OrderService(OrderRepository orderRepository, ProductClient productClient,
                        KafkaTemplate<String, Object> kafkaTemplate, TransactionTemplate tx) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.kafkaTemplate = kafkaTemplate;
        this.tx = tx;
    }

    /** Saga start: price the order (sync, circuit-breaker protected), persist PENDING, emit OrderCreated. */
    public OrderResponse placeOrder(CreateOrderRequest request) {
        List<OrderLine> lines = request.items().stream()
                .map(item -> {
                    ProductDto product = productClient.getProduct(item.skuCode());
                    return new OrderLine(product.skuCode(), product.name(), item.quantity(), product.price());
                })
                .toList();

        Order order = orderRepository.save(Order.create(request.customerEmail(), lines));

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getCustomerEmail(),
                order.getLines().stream()
                        .map(l -> new OrderItem(l.getSkuCode(), l.getQuantity(), l.getUnitPrice()))
                        .toList(),
                order.getTotalAmount());
        kafkaTemplate.send(Topics.ORDER_CREATED, order.getId(), event).join();
        log.info("Order {} created (PENDING), total {}", order.getId(), order.getTotalAmount());
        return OrderResponse.from(order);
    }

    public void confirm(String orderId) {
        Order changed = tx.execute(status -> {
            Order order = find(orderId);
            return order.confirm() ? order : null;
        });
        if (changed != null) {
            log.info("Order {} CONFIRMED", orderId);
            kafkaTemplate.send(Topics.ORDER_CONFIRMED, orderId,
                    new OrderConfirmedEvent(orderId, changed.getCustomerEmail(), changed.getTotalAmount())).join();
        }
    }

    public void cancel(String orderId, String reason) {
        Order changed = tx.execute(status -> {
            Order order = find(orderId);
            return order.cancel(reason) ? order : null;
        });
        if (changed != null) {
            log.info("Order {} CANCELLED: {}", orderId, reason);
            kafkaTemplate.send(Topics.ORDER_CANCELLED, orderId,
                    new OrderCancelledEvent(orderId, changed.getCustomerEmail(), reason)).join();
        }
    }

    public OrderResponse get(String id) {
        return OrderResponse.from(find(id));
    }

    public List<OrderResponse> list() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }

    private Order find(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
