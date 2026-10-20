package com.flashticket.reservation.repository;

import com.flashticket.reservation.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Phase 2: used by the "My bookings" page
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
