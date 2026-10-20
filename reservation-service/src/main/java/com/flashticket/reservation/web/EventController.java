package com.flashticket.reservation.web;

import com.flashticket.reservation.dto.Dtos.EventResponse;
import com.flashticket.reservation.dto.Dtos.SeatResponse;
import com.flashticket.reservation.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) { this.service = service; }

    @GetMapping
    public List<EventResponse> list() { return service.listEvents(); }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) { return service.getEvent(id); }

    @GetMapping("/{id}/seats")
    public List<SeatResponse> seats(@PathVariable Long id) { return service.getSeats(id); }
}
