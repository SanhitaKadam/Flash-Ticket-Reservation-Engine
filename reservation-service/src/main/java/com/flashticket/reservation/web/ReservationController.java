package com.flashticket.reservation.web;

import com.flashticket.reservation.dto.Dtos.ReservationRequest;
import com.flashticket.reservation.dto.Dtos.ReservationResponse;
import com.flashticket.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) { this.service = service; }

    // Hold seats (201), or 409 if any seat is already held/booked
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return service.hold(request);
    }

    // userId is a stand-in for real authentication, which arrives with the gateway in Phase 9
    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable String id, @RequestParam Long userId) {
        return service.get(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable String id, @RequestParam Long userId) {
        service.cancel(id, userId);
    }
}
