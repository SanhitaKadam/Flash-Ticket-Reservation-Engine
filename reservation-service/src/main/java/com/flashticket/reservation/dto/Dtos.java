package com.flashticket.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record EventResponse(Long id, String name, String description,
                                Instant startsAt, String venueName, String city) {}

    public record SeatResponse(Long id, String row, int number, String label,
                               BigDecimal price, String status) {}

    public record BookingRequest(@NotNull Long userId,
                                 @NotNull Long eventId,
                                 @NotEmpty List<Long> seatIds) {}

    // Phase 2: added eventName so the UI can show it without a second request
    public record BookingResponse(Long id, Long userId, Long eventId, String eventName,
                                  String status, BigDecimal totalAmount,
                                  List<String> seats, Instant createdAt) {}
}
