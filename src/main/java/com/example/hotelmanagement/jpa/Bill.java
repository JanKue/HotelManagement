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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bill", fetch = FetchType.EAGER)
    private Set<Stay> stays = new HashSet<Stay>();

    @ManyToOne(fetch = FetchType.EAGER)
    private Guest guest;
    @ManyToOne(fetch = FetchType.EAGER)
    private Hotel hotel;

    protected Bill() {}

    public Bill(Guest guest, Hotel hotel, Set<Stay> stays) throws IllegalArgumentException {

        // First, check for mismatched guests which would invalidate the transaction
        stays.forEach(stay -> {
            if (!stay.getGuest().equals(guest)) {
                throw new IllegalArgumentException("At least one of the included stays does not belong to the billed guest.");
            } else if (!stay.getHotel().equals(hotel)) {
                throw new IllegalArgumentException("At least one of the included stays does not match the billing hotel.");
            }
        });
        this.hotel = hotel;
        hotel.getBills().add(this);
        this.guest = guest;
        guest.getBills().add(this);
        this.stays = stays;
        stays.forEach(stay ->  {
            stay.setBill(this);
            if (stay.getEndDate() == null) { // if any billed stay is still ongoing, terminate it
                stay.setEndDate(LocalDate.now());
            }
        });
    }

    /**
     * Get the total duration of all stays associated with the bill.
     */
    public Long getTotalStayDuration() {
        return stays.stream()
                .map(Stay::getStayDuration)
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Get the total billed amount.
     */
    public double getTotalBilledAmount() {
        return stays.stream()
                .map(Stay::getBilledAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public Long getId() {
        return id;
    }

    public Collection<Stay> getStays() {
        return stays;
    }

    public Guest getGuest() {
        return guest;
    }

    public Hotel getHotel() {
        return hotel;
    }
}
