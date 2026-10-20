package com.flashticket.reservation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    // Unique constraint on seat_id = database-level guarantee against double booking.
    @ManyToMany
    @JoinTable(name = "booking_seats",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = "seat_id"))
    private List<Seat> seats = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(nullable = false)
    private String status = "CONFIRMED";   // becomes a proper state machine in later phases
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {}
    public Booking(User user, Event event, List<Seat> seats, BigDecimal totalAmount) {
        this.user = user; this.event = event;
        this.seats = new ArrayList<>(seats); this.totalAmount = totalAmount;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public List<Seat> getSeats() { return seats; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
