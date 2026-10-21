package com.flashticket.reservation.service;

import com.flashticket.reservation.domain.Event;
import com.flashticket.reservation.domain.Seat;
import com.flashticket.reservation.domain.SeatStatus;
import com.flashticket.reservation.dto.Dtos.BookingResponse;
import com.flashticket.reservation.dto.Dtos.ReservationRequest;
import com.flashticket.reservation.dto.Dtos.ReservationResponse;
import com.flashticket.reservation.exception.ConflictException;
import com.flashticket.reservation.exception.NotFoundException;
import com.flashticket.reservation.exception.ReservationExpiredException;
import com.flashticket.reservation.repository.EventRepository;
import com.flashticket.reservation.repository.SeatRepository;
import com.flashticket.reservation.repository.UserRepository;
import com.flashticket.reservation.service.SeatHoldStore.Hold;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Hold (Redis, fast, temporary)  ->  Confirm (PostgreSQL, permanent).
 * Redis is the fast gate that decides WHO gets a seat. PostgreSQL stays the final authority.
 */
@Service
public class ReservationService {

    private final UserRepository users;
    private final EventRepository events;
    private final SeatRepository seats;
    private final SeatHoldStore store;
    private final BookingService bookingService;
    private final long holdSeconds;

    public ReservationService(UserRepository users, EventRepository events, SeatRepository seats,
                              SeatHoldStore store, BookingService bookingService,
                              @Value("${reservation.hold-seconds:600}") long holdSeconds) {
        this.users = users; this.events = events; this.seats = seats;
        this.store = store; this.bookingService = bookingService;
        this.holdSeconds = holdSeconds;
    }

    public ReservationResponse hold(ReservationRequest req) {
        if (!users.existsById(req.userId())) {
            throw new NotFoundException("User " + req.userId() + " not found");
        }
        Event event = events.findById(req.eventId())
                .orElseThrow(() -> new NotFoundException("Event " + req.eventId() + " not found"));

        List<Long> seatIds = req.seatIds().stream().distinct().sorted().toList();
        List<Seat> found = seats.findByEventIdAndIdIn(event.getId(), seatIds);
        if (found.size() != seatIds.size()) {
            throw new NotFoundException("One or more seats do not exist for this event");
        }
        for (Seat s : found) {
            if (s.getStatus() == SeatStatus.BOOKED) {
                throw new ConflictException("Seat " + s.label() + " is already booked");
            }
        }

        Instant expiresAt = Instant.now().plusSeconds(holdSeconds);
        Hold hold = new Hold(UUID.randomUUID().toString(), req.userId(), event.getId(), seatIds, expiresAt);

        // The race happens HERE: many requests arrive, Redis lets exactly one through.
        Long conflictSeatId = store.tryHold(hold, holdSeconds);
        if (conflictSeatId != null) {
            String label = found.stream().filter(s -> s.getId().equals(conflictSeatId))
                    .map(Seat::label).findFirst().orElse("A selected seat");
            throw new ConflictException("Seat " + label + " is currently held by another customer");
        }
        return toResponse(hold, found, event.getName());
    }

    public ReservationResponse get(String reservationId, Long userId) {
        Hold hold = ownedHold(reservationId, userId);
        List<Seat> found = seats.findAllById(hold.seatIds());
        String eventName = events.findById(hold.eventId()).map(Event::getName).orElse("");
        return toResponse(hold, found, eventName);
    }

    public void cancel(String reservationId, Long userId) {
        // Cancelling something already gone is fine (idempotent), so no error when missing.
        store.find(reservationId).ifPresent(hold -> {
            if (!hold.userId().equals(userId)) throw new NotFoundException("Reservation not found");
            store.release(hold);
        });
    }

    /** Turns a valid hold into a permanent booking. */
    public BookingResponse confirm(String reservationId, Long userId) {
        Hold hold = ownedHold(reservationId, userId);
        if (!store.seatsStillHeldBy(hold)) {
            throw new ReservationExpiredException("Your hold has expired. Please select your seats again.");
        }
        // One ACID transaction in PostgreSQL (row locks + unique constraint = final safety net).
        BookingResponse booking = bookingService.createFromSeats(userId, hold.eventId(), hold.seatIds());
        // Reservation is used up. Seat keys expire by themselves; until then the seats are
        // BOOKED in PostgreSQL anyway, which takes priority over any Redis hold.
        store.deleteReservation(hold.id());
        return booking;
    }

    private Hold ownedHold(String reservationId, Long userId) {
        Hold hold = store.find(reservationId).orElseThrow(() ->
                new ReservationExpiredException("Reservation not found or expired"));
        if (!hold.userId().equals(userId)) {
            throw new NotFoundException("Reservation not found");
        }
        return hold;
    }

    private ReservationResponse toResponse(Hold hold, List<Seat> seatList, String eventName) {
        List<Seat> sorted = seatList.stream().sorted(Comparator.comparing(Seat::getId)).toList();
        BigDecimal total = sorted.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        long remaining = Math.max(0, Duration.between(Instant.now(), hold.expiresAt()).getSeconds());
        return new ReservationResponse(hold.id(), hold.userId(), hold.eventId(), eventName,
                sorted.stream().map(Seat::label).toList(), total, hold.expiresAt(), remaining);
    }
}
