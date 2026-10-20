package com.flashticket.reservation.service;

import com.flashticket.reservation.domain.*;
import com.flashticket.reservation.dto.Dtos.BookingRequest;
import com.flashticket.reservation.dto.Dtos.BookingResponse;
import com.flashticket.reservation.exception.ConflictException;
import com.flashticket.reservation.exception.NotFoundException;
import com.flashticket.reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingService {

    private final UserRepository users;
    private final EventRepository events;
    private final SeatRepository seats;
    private final BookingRepository bookings;

    public BookingService(UserRepository users, EventRepository events,
                          SeatRepository seats, BookingRepository bookings) {
        this.users = users; this.events = events;
        this.seats = seats; this.bookings = bookings;
    }

    @Transactional
    public BookingResponse create(BookingRequest req) {
        User user = users.findById(req.userId())
                .orElseThrow(() -> new NotFoundException("User " + req.userId() + " not found"));
        Event event = events.findById(req.eventId())
                .orElseThrow(() -> new NotFoundException("Event " + req.eventId() + " not found"));

        var requestedIds = new HashSet<>(req.seatIds());

        // 1. Lock the seat rows. Other transactions wanting these seats WAIT here.
        List<Seat> locked = seats.lockSeats(requestedIds, event.getId());
        if (locked.size() != requestedIds.size()) {
            throw new NotFoundException("One or more seats do not exist for this event");
        }

        // 2. Check they are still free (we hold the lock, so this check is reliable).
        for (Seat s : locked) {
            if (s.getStatus() != SeatStatus.AVAILABLE) {
                throw new ConflictException("Seat " + s.label() + " is already booked");
            }
        }

        // 3. Mark booked and record the booking, all in one ACID transaction.
        locked.forEach(s -> s.setStatus(SeatStatus.BOOKED));
        BigDecimal total = locked.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Booking saved = bookings.save(new Booking(user, event, locked, total));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        return toResponse(bookings.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found")));
    }

    // Phase 2
    @Transactional(readOnly = true)
    public List<BookingResponse> listForUser(Long userId) {
        return bookings.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getUser().getId(), b.getEvent().getId(),
                b.getEvent().getName(), b.getStatus(), b.getTotalAmount(),
                b.getSeats().stream().map(Seat::label).toList(), b.getCreatedAt());
    }
}
