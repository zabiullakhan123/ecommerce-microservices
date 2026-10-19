package com.shop.product;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds a small catalogue on first start so the demo works out of the box. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final ProductRepository repository;

    public DataSeeder(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Product("IPHONE-15", "iPhone 15", "128 GB, Black", new BigDecimal("79999.00")),
                new Product("PIXEL-9", "Google Pixel 9", "256 GB, Obsidian", new BigDecimal("69999.00")),
                new Product("AIRPODS-PRO", "AirPods Pro 2", "USB-C case", new BigDecimal("24999.00")),
                new Product("MACBOOK-AIR", "MacBook Air M3", "13-inch, 16 GB RAM", new BigDecimal("114999.00")),
                new Product("USB-C-CABLE", "USB-C Cable", "1 m braided", new BigDecimal("999.00"))));
        log.info("Seeded {} products", repository.count());
    }
}
