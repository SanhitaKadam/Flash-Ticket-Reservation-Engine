package com.flashticket.reservation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "row_label", "seat_number"}))
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;
    @Column(name = "row_label", nullable = false, length = 3)
    private String rowLabel;
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    protected Seat() {}
    public Seat(Event event, String rowLabel, int seatNumber, BigDecimal price) {
        this.event = event; this.rowLabel = rowLabel;
        this.seatNumber = seatNumber; this.price = price;
    }

    public String label() { return rowLabel + seatNumber; }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getRowLabel() { return rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public BigDecimal getPrice() { return price; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
}
