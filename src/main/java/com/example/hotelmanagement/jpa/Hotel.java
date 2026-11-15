package com.example.hotelmanagement.jpa;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Guest> registeredGuests = new HashSet<>();

    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
    private Set<Stay> stays = new HashSet<>();
    @OneToMany(mappedBy = "hotel", fetch = FetchType.EAGER)
    private Set<Bill> bills = new HashSet<>();

    protected Hotel() {}

    public Hotel(String name) {
        this.name = name;
    }

    public void registerGuest(Guest guest) {
        registeredGuests.add(guest);
        guest.getHotelsRegisteredAt().add(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Guest> getRegisteredGuests() {
        return registeredGuests;
    }

    public Collection<Stay> getStays() {
        return stays;
    }

    public Collection<Bill> getBills() {
        return bills;
    }
}
