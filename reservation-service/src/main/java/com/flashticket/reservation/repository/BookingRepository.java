package com.flashticket.reservation.repository;

import com.flashticket.reservation.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {}
