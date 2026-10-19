package com.shop.order.client;

import java.math.BigDecimal;

import com.shop.order.exception.ProductNotFoundException;
import com.shop.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Synchronous call to product-service, protected by Resilience4j:
 * Retry (outer, owns the fallback) -> CircuitBreaker (inner) -> HTTP call.
 * <ul>
 *   <li>Timeouts / 5xx are retried with exponential backoff and counted by the breaker.</li>
 *   <li>When the breaker is OPEN it fails fast (CallNotPermittedException, not retried).</li>
 *   <li>A 404 is a business error: ignored by both and passed straight through.</li>
 * </ul>
 */
@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    public record ProductDto(String skuCode, String name, BigDecimal price) {
    }

    private final RestClient restClient;

    public ProductClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://product-service").build();
    }

    @Retry(name = "productService", fallbackMethod = "productFallback")
    @CircuitBreaker(name = "productService")
    public ProductDto getProduct(String skuCode) {
        try {
            return restClient.get()
                    .uri("/api/products/{sku}", skuCode)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(skuCode);
        }
    }

    public ProductDto productFallback(String skuCode, Throwable t) {
        if (t instanceof ProductNotFoundException notFound) {
            throw notFound;
        }
        log.warn("product-service call failed for {}: {}", skuCode, t.toString());
        throw new ServiceUnavailableException("product-service is unavailable, please retry later");
    }
}
