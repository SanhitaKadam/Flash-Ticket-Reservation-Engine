package com.flashticket.reservation.web;

import com.flashticket.reservation.dto.Dtos.BookingRequest;
import com.flashticket.reservation.dto.Dtos.BookingResponse;
import com.flashticket.reservation.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) { return service.get(id); }

    // Phase 2: GET /api/bookings?userId=1
    @GetMapping
    public List<BookingResponse> list(@RequestParam Long userId) {
        return service.listForUser(userId);
    }
}
