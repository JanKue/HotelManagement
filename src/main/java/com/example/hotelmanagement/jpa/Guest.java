/*
    Copyright (C) 2026 Jan Küblbeck

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
