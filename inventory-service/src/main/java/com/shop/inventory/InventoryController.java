package com.shop.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    public record StockResponse(String skuCode, int available, int reserved) {
        static StockResponse from(InventoryItem i) {
            return new StockResponse(i.getSkuCode(), i.getAvailableQuantity(), i.getReservedQuantity());
        }
    }

    private final InventoryItemRepository repository;

    public InventoryController(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<StockResponse> list() {
        return repository.findAll().stream().map(StockResponse::from).toList();
    }

    @GetMapping("/{skuCode}")
    public StockResponse get(@PathVariable String skuCode) {
        return repository.findBySkuCode(skuCode).map(StockResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown SKU " + skuCode));
    }
}
