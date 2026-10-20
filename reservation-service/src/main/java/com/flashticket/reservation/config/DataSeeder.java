package com.flashticket.reservation.config;

import com.flashticket.reservation.domain.*;
import com.flashticket.reservation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final VenueRepository venues;
    private final EventRepository events;
    private final SeatRepository seats;

    public DataSeeder(UserRepository users, VenueRepository venues,
                      EventRepository events, SeatRepository seats) {
        this.users = users; this.venues = venues;
        this.events = events; this.seats = seats;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (venues.count() > 0) return;

        users.save(new User("Demo User", "demo@example.com"));
        Venue venue = venues.save(new Venue("Skyline Arena", "Mumbai"));

        Event concert = events.save(new Event("Neon Skies Live",
                "An open-air electronic music night.",
                Instant.now().plus(30, ChronoUnit.DAYS), venue));
        Event match = events.save(new Event("City Derby Final",
                "The season-deciding football match.",
                Instant.now().plus(45, ChronoUnit.DAYS), venue));

        for (Event e : new Event[]{concert, match}) {
            for (char row = 'A'; row <= 'E'; row++) {
                for (int n = 1; n <= 10; n++) {
                    BigDecimal price = new BigDecimal(row <= 'B' ? "2500.00" : "1200.00");
                    seats.save(new Seat(e, String.valueOf(row), n, price));
                }
            }
        }
    }
}
