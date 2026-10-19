package com.shop.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String skuCode;

    private int availableQuantity;

    /** Held for orders whose payment has not completed yet. */
    private int reservedQuantity;

    @Version
    private Long version;

    protected InventoryItem() {
    }

    public InventoryItem(String skuCode, int availableQuantity) {
        this.skuCode = skuCode;
        this.availableQuantity = availableQuantity;
    }

    public boolean canReserve(int quantity) {
        return availableQuantity >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock for " + skuCode);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    /** Compensation: give held stock back. */
    public void release(int quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    /** Payment succeeded: held stock is now sold. */
    public void commit(int quantity) {
        reservedQuantity -= quantity;
    }

    public Long getId() { return id; }
    public String getSkuCode() { return skuCode; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
}
