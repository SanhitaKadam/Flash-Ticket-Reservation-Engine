package com.flashticket.reservation.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String city;

    protected Venue() {}
    public Venue(String name, String city) { this.name = name; this.city = city; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
}
