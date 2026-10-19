package com.shop.inventory;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InventoryItemRepository repository;

    public DataSeeder(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new InventoryItem("IPHONE-15", 50),
                    new InventoryItem("PIXEL-9", 30),
                    new InventoryItem("AIRPODS-PRO", 100),
                    new InventoryItem("MACBOOK-AIR", 5),
                    new InventoryItem("USB-C-CABLE", 500)));
        }
    }
}
