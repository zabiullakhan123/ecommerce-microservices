package com.shop.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByOrderId(String orderId);
    List<Reservation> findByOrderIdAndStatus(String orderId, Reservation.Status status);
}
