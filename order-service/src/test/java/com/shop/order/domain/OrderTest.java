package com.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderTest {

    private static Order newOrder() {
        return Order.create("a@b.com", List.of(
                new OrderLine("IPHONE-15", "iPhone 15", 2, new BigDecimal("100.00")),
                new OrderLine("USB-C-CABLE", "Cable", 3, new BigDecimal("10.50"))));
    }

    @Test
    void newOrderIsPendingWithComputedTotal() {
        Order order = newOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("231.50");
    }

    @Test
    void confirmIsIdempotent() {
        Order order = newOrder();
        assertThat(order.confirm()).isTrue();
        assertThat(order.confirm()).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void lateCancelAfterConfirmIsIgnored() {
        Order order = newOrder();
        order.confirm();
        assertThat(order.cancel("too late")).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cancelRecordsReason() {
        Order order = newOrder();
        assertThat(order.cancel("Insufficient stock")).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getFailureReason()).isEqualTo("Insufficient stock");
    }

    @Test
    void emptyOrderIsRejected() {
        assertThatThrownBy(() -> Order.create("a@b.com", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
