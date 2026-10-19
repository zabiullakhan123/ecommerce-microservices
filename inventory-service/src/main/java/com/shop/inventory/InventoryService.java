package com.shop.inventory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.shop.common.events.OrderCreatedEvent;
import com.shop.common.events.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public record ReservationResult(boolean success, String reason) {
        static ReservationResult ok() { return new ReservationResult(true, null); }
        static ReservationResult rejected(String reason) { return new ReservationResult(false, reason); }
    }

    private final InventoryItemRepository itemRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(InventoryItemRepository itemRepository, ReservationRepository reservationRepository) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
    }

    /** All-or-nothing reservation of every line in the order. Idempotent per orderId. */
    @Transactional
    public ReservationResult reserve(OrderCreatedEvent event) {
        if (!reservationRepository.findByOrderId(event.orderId()).isEmpty()) {
            log.info("Order {} already reserved; skipping duplicate event", event.orderId());
            return ReservationResult.ok();
        }

        Map<String, Integer> requested = event.items().stream()
                .collect(Collectors.toMap(OrderItem::skuCode, OrderItem::quantity, Integer::sum, TreeMap::new));
        Map<String, InventoryItem> stock = itemRepository.findAllBySkuCodeForUpdate(requested.keySet()).stream()
                .collect(Collectors.toMap(InventoryItem::getSkuCode, Function.identity()));

        // Validate everything first, so we never partially reserve.
        for (Map.Entry<String, Integer> line : requested.entrySet()) {
            InventoryItem item = stock.get(line.getKey());
            if (item == null) {
                return ReservationResult.rejected("No inventory record for " + line.getKey());
            }
            if (!item.canReserve(line.getValue())) {
                return ReservationResult.rejected("Insufficient stock for %s (available %d, requested %d)"
                        .formatted(line.getKey(), item.getAvailableQuantity(), line.getValue()));
            }
        }

        requested.forEach((sku, qty) -> {
            stock.get(sku).reserve(qty);
            reservationRepository.save(new Reservation(event.orderId(), sku, qty));
        });
        log.info("Reserved stock for order {}: {}", event.orderId(), requested);
        return ReservationResult.ok();
    }

    /** Saga compensation, triggered when payment fails. */
    @Transactional
    public void release(String orderId) {
        settle(orderId, Reservation.Status.RELEASED);
    }

    /** Payment succeeded: turn the hold into a sale. */
    @Transactional
    public void commit(String orderId) {
        settle(orderId, Reservation.Status.COMMITTED);
    }

    private void settle(String orderId, Reservation.Status target) {
        List<Reservation> held = reservationRepository.findByOrderIdAndStatus(orderId, Reservation.Status.RESERVED);
        if (held.isEmpty()) {
            return; // nothing reserved, or already settled (idempotent)
        }
        Map<String, InventoryItem> stock = itemRepository
                .findAllBySkuCodeForUpdate(held.stream().map(Reservation::getSkuCode).toList()).stream()
                .collect(Collectors.toMap(InventoryItem::getSkuCode, Function.identity()));
        for (Reservation r : held) {
            InventoryItem item = stock.get(r.getSkuCode());
            if (target == Reservation.Status.RELEASED) {
                item.release(r.getQuantity());
            } else {
                item.commit(r.getQuantity());
            }
            r.setStatus(target);
        }
        log.info("Order {} reservations -> {}", orderId, target);
    }
}
