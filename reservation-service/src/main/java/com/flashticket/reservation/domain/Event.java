package com.flashticket.reservation.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    protected Event() {}
    public Event(String name, String description, Instant startsAt, Venue venue) {
        this.name = name; this.description = description;
        this.startsAt = startsAt; this.venue = venue;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Instant getStartsAt() { return startsAt; }
    public Venue getVenue() { return venue; }
}
