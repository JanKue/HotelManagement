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
