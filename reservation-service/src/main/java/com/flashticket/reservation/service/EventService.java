package com.flashticket.reservation.service;

import com.flashticket.reservation.domain.Event;
import com.flashticket.reservation.domain.Seat;
import com.flashticket.reservation.domain.SeatStatus;
import com.flashticket.reservation.dto.Dtos.EventResponse;
import com.flashticket.reservation.dto.Dtos.SeatResponse;
import com.flashticket.reservation.exception.NotFoundException;
import com.flashticket.reservation.repository.EventRepository;
import com.flashticket.reservation.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository events;
    private final SeatRepository seats;
    private final SeatHoldStore holds;

    public EventService(EventRepository events, SeatRepository seats, SeatHoldStore holds) {
        this.events = events;
        this.seats = seats;
        this.holds = holds;
    }

    public List<EventResponse> listEvents() {
        return events.findAll().stream().map(this::toResponse).toList();
    }

    public EventResponse getEvent(Long id) {
        return toResponse(events.findById(id)
                .orElseThrow(() -> new NotFoundException("Event " + id + " not found")));
    }

    /** Seat status = PostgreSQL (BOOKED) combined with Redis (HELD). BOOKED wins. */
    public List<SeatResponse> getSeats(Long eventId) {
        if (!events.existsById(eventId)) {
            throw new NotFoundException("Event " + eventId + " not found");
        }
        List<Seat> all = seats.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId);
        Set<Long> held = holds.heldSeatIds(eventId, all.stream().map(Seat::getId).toList());
        return all.stream()
                .map(s -> new SeatResponse(s.getId(), s.getRowLabel(), s.getSeatNumber(),
                        s.label(), s.getPrice(), statusOf(s, held)))
                .toList();
    }

    private String statusOf(Seat s, Set<Long> held) {
        if (s.getStatus() == SeatStatus.BOOKED) return "BOOKED";
        return held.contains(s.getId()) ? "HELD" : "AVAILABLE";
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(e.getId(), e.getName(), e.getDescription(), e.getStartsAt(),
                e.getVenue().getName(), e.getVenue().getCity());
    }
}
