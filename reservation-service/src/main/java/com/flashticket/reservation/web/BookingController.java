package com.flashticket.reservation.web;

import com.flashticket.reservation.dto.Dtos.BookingRequest;
import com.flashticket.reservation.dto.Dtos.BookingResponse;
import com.flashticket.reservation.service.BookingService;
import com.flashticket.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService service;
    private final ReservationService reservations;

    public BookingController(BookingService service, ReservationService reservations) {
        this.service = service;
        this.reservations = reservations;
    }

    // Phase 3: a booking is now created by CONFIRMING a hold: { "reservationId": "...", "userId": 1 }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        return reservations.confirm(request.reservationId(), request.userId());
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public List<BookingResponse> list(@RequestParam Long userId) {
        return service.listForUser(userId);
    }
}
