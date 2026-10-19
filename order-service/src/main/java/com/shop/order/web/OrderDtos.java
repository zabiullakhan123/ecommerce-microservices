package com.shop.order.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotBlank @Email String customerEmail,
            @NotEmpty List<@Valid Item> items) {

        public record Item(@NotBlank String skuCode, @Min(1) int quantity) {
        }
    }

    public record OrderResponse(
            String id,
            String customerEmail,
            OrderStatus status,
            BigDecimal totalAmount,
            String failureReason,
            List<Line> items,
            Instant createdAt,
            Instant updatedAt) {

        public record Line(String skuCode, String productName, int quantity, BigDecimal unitPrice) {
        }

        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getStatus(), o.getTotalAmount(),
                    o.getFailureReason(),
                    o.getLines().stream()
                            .map(l -> new Line(l.getSkuCode(), l.getProductName(), l.getQuantity(), l.getUnitPrice()))
                            .toList(),
                    o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
