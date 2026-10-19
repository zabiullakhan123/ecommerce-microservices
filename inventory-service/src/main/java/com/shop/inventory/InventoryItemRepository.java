package com.shop.inventory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySkuCode(String skuCode);

    /** Row-locks the items (ordered by SKU to avoid deadlocks) so concurrent orders can't oversell. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.skuCode in :skus order by i.skuCode")
    List<InventoryItem> findAllBySkuCodeForUpdate(@Param("skus") Collection<String> skus);
}
