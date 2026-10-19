package com.shop.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record CreateProductRequest(
            @NotBlank String skuCode,
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price) {
    }

    public record ProductResponse(String skuCode, String name, String description, BigDecimal price) {
        static ProductResponse from(Product p) {
            return new ProductResponse(p.getSkuCode(), p.getName(), p.getDescription(), p.getPrice());
        }
    }
}
