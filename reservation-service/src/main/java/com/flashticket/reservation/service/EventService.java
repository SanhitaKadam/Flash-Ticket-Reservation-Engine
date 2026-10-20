package com.flashticket.reservation.service;

import com.flashticket.reservation.domain.Event;
import com.flashticket.reservation.dto.Dtos.EventResponse;
import com.flashticket.reservation.dto.Dtos.SeatResponse;
import com.flashticket.reservation.exception.NotFoundException;
import com.flashticket.reservation.repository.EventRepository;
import com.flashticket.reservation.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository events;
    private final SeatRepository seats;

    public EventService(EventRepository events, SeatRepository seats) {
        this.events = events;
        this.seats = seats;
    }

    public List<EventResponse> listEvents() {
        return events.findAll().stream().map(this::toResponse).toList();
    }

    public EventResponse getEvent(Long id) {
        return toResponse(events.findById(id)
                .orElseThrow(() -> new NotFoundException("Event " + id + " not found")));
    }

    public List<SeatResponse> getSeats(Long eventId) {
        if (!events.existsById(eventId)) {
            throw new NotFoundException("Event " + eventId + " not found");
        }
        return seats.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId).stream()
                .map(s -> new SeatResponse(s.getId(), s.getRowLabel(), s.getSeatNumber(),
                        s.label(), s.getPrice(), s.getStatus().name()))
                .toList();
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(e.getId(), e.getName(), e.getDescription(), e.getStartsAt(),
                e.getVenue().getName(), e.getVenue().getCity());
    }
}
