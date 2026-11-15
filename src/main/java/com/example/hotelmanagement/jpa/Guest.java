package com.example.hotelmanagement.jpa;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate dateOfBirth;
    private String address;

    @ManyToMany(mappedBy = "registeredGuests", fetch = FetchType.EAGER)
    private Set<Hotel> hotelsRegisteredAt = new HashSet<>();

    @OneToMany(mappedBy = "guest", fetch = FetchType.EAGER)
    private Set<Stay> stays = new HashSet<>();

    @OneToMany(mappedBy = "guest", fetch = FetchType.EAGER)
    private Set<Bill> bills = new HashSet<>();

    protected Guest() {}

    public Guest(String name) {
        this.name = name;
        this.dateOfBirth = LocalDate.now(); // newborn guest
    }

    public Guest(String name, LocalDate dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public Guest(String name, LocalDate dateOfBirth, String address) {
        this(name , dateOfBirth);
        this.address = address;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        return age.getYears();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Collection<Hotel> getHotelsRegisteredAt() {
        return hotelsRegisteredAt;
    }

    public Collection<Stay> getStays() {
        return stays;
    }

    public Collection<Bill> getBills() {
        return bills;
    }
}
