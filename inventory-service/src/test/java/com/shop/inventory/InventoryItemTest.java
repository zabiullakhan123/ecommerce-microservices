package com.shop.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InventoryItemTest {

    @Test
    void reserveMovesStockFromAvailableToReserved() {
        InventoryItem item = new InventoryItem("SKU", 10);
        item.reserve(3);
        assertThat(item.getAvailableQuantity()).isEqualTo(7);
        assertThat(item.getReservedQuantity()).isEqualTo(3);
    }

    @Test
    void releaseIsTheCompensationOfReserve() {
        InventoryItem item = new InventoryItem("SKU", 10);
        item.reserve(4);
        item.release(4);
        assertThat(item.getAvailableQuantity()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isZero();
    }

    @Test
    void commitConsumesReservedStock() {
        InventoryItem item = new InventoryItem("SKU", 10);
        item.reserve(2);
        item.commit(2);
        assertThat(item.getAvailableQuantity()).isEqualTo(8);
        assertThat(item.getReservedQuantity()).isZero();
    }

    @Test
    void cannotOversell() {
        InventoryItem item = new InventoryItem("SKU", 1);
        assertThatThrownBy(() -> item.reserve(2)).isInstanceOf(IllegalStateException.class);
    }
}
