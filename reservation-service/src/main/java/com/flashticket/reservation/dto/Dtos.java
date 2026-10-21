package com.flashticket.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record EventResponse(Long id, String name, String description,
                                Instant startsAt, String venueName, String city) {}

    // status is AVAILABLE, HELD (in Redis) or BOOKED (in PostgreSQL)
    public record SeatResponse(Long id, String row, int number, String label,
                               BigDecimal price, String status) {}

    // Phase 3: step 1, hold seats
    public record ReservationRequest(@NotNull Long userId,
                                     @NotNull Long eventId,
                                     @NotEmpty @Size(max = 6) List<Long> seatIds) {}

    public record ReservationResponse(String id, Long userId, Long eventId, String eventName,
                                      List<String> seats, BigDecimal totalAmount,
                                      Instant expiresAt, long secondsRemaining) {}

    // Phase 3: step 2, confirm a hold into a permanent booking (POST /api/bookings)
    public record BookingRequest(@NotBlank @Size(max = 64) String reservationId,
                                 @NotNull Long userId) {}

    public record BookingResponse(Long id, Long userId, Long eventId, String eventName,
                                  String status, BigDecimal totalAmount,
                                  List<String> seats, Instant createdAt) {}
}
